<#import "freemarker/main-template.ftl" as u>

<@u.page>
<div class="page-header top5">
    <div class="row text-center">
        <h1 class="text-center">Tired of checked exceptions?</h1>
    </div>
    <div class="row">
        <p>
            This lombok spinoff project consists of a hack that only works in javac - not eclipse or any other IDE.<br/>
            It will completely disable the notion of checked exceptions. You may throw any exception anywhere, and you
            may
            also catch any exception anywhere. In standard javac, you may not catch a checked exception that is not
            declared
            as thrown by at least 1 statement in your try block, unless it is <code>Exception</code> or
            <code>Throwable</code>.<br/>
            This restriction is lifted as well.
        </p>
    </div>
    <div class="row">
        <h2 class="text-center">Usage</h2>

        <p>
            Just make sure <code>disableCheckedExceptions-alpha.jar</code> is on the classpath as you compile. For
            example:<br/>
            <code>javac -cp disableCheckedExceptions-alpha.jar MySource.java</code>
        </p>

        <p>
            Ready to try it out? download it here: <a href="downloads/disableCheckedExceptions-alpha.jar">disableCheckedExceptions-alpha.jar</a>
        </p>

        <p>
            Want to know how its done? Grab the lombok repository <a
                href="http://github.com/rzwitserloot/lombok/tree/disableCheckedExceptions">here on github</a>,
            and look in the <code>experimental</code> directory.
        </p>

    </div>
</div>
</div>
</@u.page>
