Generic Web API
---------------

A library for defining a REST web API as a Java ``enum`` and calling
that API using POJOs for arguments and return values.  Uses an
[asynchronous Netty-based HTTP library](https://github.com/timboudreau/netty-http-client)
for HTTP communication, so few threads are used.

To define a web API, simply create an enum which implements ``WebCallEnum`` as
described below;  use a builder to define what the call should look like.

To call a web API, you invoke that web API, passing in the arguments it needs
as a free-form array of objects plus a callback which will be invoked with the
result.  E.g.

public enum TestAPI implements WebCallEnum {
    HELLO_WORLD(new WebCallBuilder()
                        .method(Method.GET)
                        .addRequiredType(UserId.class)
                        .withDecorator(DisplayName.class, ParameterFromClassNameAndToStringCamelCase.class)
                        .path("/users/{{userid}}/hello").responseType(Map.class)),
    ...
}

That defines an HTTP call which issues a ``GET`` request;  a portion of the URL
is templated - it will be gotten from calling ``toString()`` on a ``UserId`` object
(i.e. some application specific class).  The API wants a URL parameter called
"displayName" - we map that to ``toString()`` on an instance of ``DisplayName`` - 
this could be any type of our own creation (and we could unmarshal it some other
way than ``toString()``).  The response will be unmarshalled from JSON into a Map - 
but we could use any type Jackson can handle here.

So to call it, we do 

	    Invoker&lt;TestAPI&gt; invoker = Invoker.create(URL.parse("http://server.example"), 
		TestAPI.class);

To receive a call back with the response, we implement the ``Callback`` interface,
and then call

	ResponseFuture f = invoker.call(TestAPI.HELLO_WORLD, myCallback, 
		    new DisplayName("Tim Boudreau"), new UserId("tim"));

## Background

There is lots of support for writing REST APIs in Java;  there
are fewer options for writing REST <i>clients</i> (some existing
ones are described 
[here](http://stackoverflow.com/questions/221442/rest-clients-for-java)).

This library is the result of its author needing to write a REST client
one too many times and not being satisfied with the options, and wanting
one with an asynchronous, callback-based API - if you are writing an
asynchronous web service which needs to call another web service, and calling the 
remote service is not asynchronous too, then you instantly go from
having a massively scalable web API to having one which is blocked
doing I/O and scales worse than old-fashioned threaded alternatives.

Calling REST APIs is simple enough that it's commonplace to do it with
plain old Java code.  But what inevitably happens is that an entire
codebase gets littered with boilerplate such as setting content types,
literal paths and URLs and similar noise-code.  Then one day the API
changes, and finding all of the places the code must be updated is a long,
expensive and painful job that usually misses some cases.

The idea here is to use some of Guice's injection features under the
hood to make it very simple to define elements of a web API and pass
arguments to construct a web call.  So you deal in objects, and those
get translated into an HTTP call in a well-defined way in a well-defined
place, but you don't have to write a lot of code to do those calls.

It means that making a web call
involves no repetitive boilerplate, and for most common cases, things
like interpreting the response are handled.  More importantly, it 
keeps things like URL paths all located in one place, so refactoring of
a web api doesn't mean grepping your source code.  To call a web api,
you simply pass an enum constant representing that call to an
<a href="Invoker.html"><code>Invoker</code></a>, and some 
objects to flesh out the call's URL, headers and or body - and you can
literally pass as many objects in whatever order as you want - the 
signature is 
        
	invoker.call(Enum&lt;?&gt; call, Callback&lt;T&gt; callback, Object... args)</code>.

##Defining the Web API you want to call
Your web api starts with a Java <code>Enum</code> which defines all of
the calls in the API.  The enum must implement 
``WebCallEnum`` which has a 
single method, <code>get()</code> which returns an instance of 
<code>WebCall</code>.  The enum serves as a handy way to reference
API calls, and a way that the system (which uses Guice to bind object
types) knows the entire list of types which might be used to construct
web calls.
<p/>
Here's a sample API with two calls, one which says hello and one which
echoes back the request body sent to it.

    public enum TestAPI implements WebCallEnum {

	    HELLO_WORLD(new WebCallBuilder()
		                .method(Method.GET)
		                .addRequiredType(UserId.class)
		                .withDecorator(DisplayName.class, ParameterFromClassNameAndToStringCamelCase.class)
		                .path("/users/{{userid}}/hello").responseType(Map.class)),
	    ECHO(new WebCallBuilder()
		                .method(Method.POST)
		                .addRequiredType(String.class)
		                .addRequiredType(UserId.class)
		                .withDecorator(String.class, BodyFromString.class)
		                .path("/users/{{userid}}/echo").responseType(String.class));

	    private final WebCall call;
	    TestAPI(WebCallBuilder bldr) {
		call = bldr.id(this).build();
	    }

	    public WebCall get() {
		return call;
	    }
	}

##Calling The Web API
How do we use it?  Very simply.  First, we need an instance of an
``Invoker``;  you can either
create one using Guice (you need to bind a base URL and an HttpClient),
or use a static method to have it do that for you:
	    Invoker&lt;TestAPI&gt; invoker = Invoker.create(URL.parse("http://server.example"), 
		TestAPI.class);

		Then we need a callback which will be
		called when our call completes - it gets passed the result and can
		do whatever you need to with it:
		<pre>
	Callback&lt;Map&gt; c = new Callback&lt;Map&gt;(Map.class) {
	    public void success(Map object) {
		System.out.println("YAY: " + object);
	    }
	    public void fail(HttpResponseStatus status, ByteBuf bytes) {
		System.out.println("FAILED WITH " + status);
	    }
	};

Then we simply pass the callback, and some arguments, to the invoker:

	ResponseFuture f = invoker.call(TestAPI.HELLO_WORLD, c, 
		    new DisplayName("Tim Boudreau"), new UserId("tim"));

When the request completes, or if it fails, our callback will be called
(there are a few other methods, such as exception handling, which can
optionally be overridden).

Under the hood, what happens is this:

 * We look up the <i>path template</i> tied to this web call - in
this case ``/users/{{userid}}/hello``.  The ``{{userid}}``
part will be substituted.  Fancy substitutions are possible by
writing your own ``Interpolator``,
but "userid" matches the lower case name of the class ``UserId`` - 
and we passed one in.  So its <code>toString()</code> is called, and
"{{userid}}" is replaced with "tim".

 * When we created the call, you may have noticed the line
``.withDecorator(DisplayName.class, ParameterFromClassNameAndToStringCamelCase.class)``.
We have a <code>DisplayName</code> class which is also a wrapper
for a string.  ``ParameterFromClassNameAndToStringCamelCase``
is a ``Decorator`` which
ships with this library.  It simply takes the simple class name and
lower-cases it, and uses <code>toString()</code> on the value to 
construct a query parameter.  So the actual URL we will call is
now ``http://server.example/users/tim/hello?displayName=Tim+Boudreau``

 * An HTTP request with an empty body is constructed and sent to
the server.

 * Our callback takes a ``Map`` as its parameter.  So the
code will take the response body, interpret it as JSON and 
convert the content to a ``Map`` (using Jackson) and
pass that to our callback.  We could also have asked for the content
as an ``Image``, ``String``, ``ByteBuf`` (from Netty), ``byte[]`` or ``InputStream``, and it
could have be unmarshalled transparently as any of those things;
or if it is a type which Jackson can unmarshal from JSON, that
can be done automatically.  For custom unmarshalling, you simply
implement ``Interpreter``
and add that to your ``WebCall``.

### ResponseFuture

Making an API call returns a ``ResponseFuture``
which you can use to cancel the call, or attach listeners to for events such
as the response being sent, the headers being sent back, or even individual
content chunks arriving.

It also has two ``await()`` methods which will block the calling
thread until the call has completed.  They are useful in unit tests
and things like that; *please don't use them in non-test code!*
For an overview of why Future is an antipattern, see this abstract <i>
[Why There’s No Future in Java Futures](https://oracleus.activeevents.com/connect/sessionDetail.ww?SESSION_ID=6385)

        Essentially, the number one rule of using an asynchronous, message-based
        API is don't block, ever.

## Conclusions
The result is that calling a web api is dirt-simple, scalable, and 
relatively intuitive;  and since the entire definition of the API lives
in one place, refactoring is simple - no searching code or guessing
required.


