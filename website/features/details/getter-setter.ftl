<#import "../../freemarker/main-template.ftl" as u>

<@u.page>
<div class="page-header top5">
    <div class="row text-center">
        <div class="header-group">
            <h1>@Getter and @Setter</h1>

            <h3>Never write <code>public int getFoo() {return foo;}</code> again.</h3>
        </div>
    </div>
    <div class="row">

        <h3>Overview</h3>

        <p>
            You can annotate any field with <code>@Getter</code> and/or <code>@Setter</code>, to let lombok generate
            the default getter/setter automatically.<br/>
            A default getter simply returns the field, and is named <code>getFoo</code> if the field is called
            <code>foo</code> (or <code>isFoo</code> if the field's type is <code>boolean</code>). A default setter
            is named <code>setFoo</code> if the field is called <code>foo</code>, returns <code>void</code>, and
            takes 1 parameter of the same type as the field. It simply sets the field to this value.
        </p>

        <p>
            The generated getter/setter method will be <code>public</code> unless you explicitly specify an
            <code>AccessLevel</code>,
            as shown in the example below. Legal access levels are <code>PUBLIC</code>, <code>PROTECTED</code>,
            <code>PACKAGE</code>, and <code>PRIVATE</code>.
        </p>

        <p>
            You can also put a <code>@Getter</code> and/or <code>@Setter</code> annotation on a class. In that case,
            it's as if you annotate all the non-static fields in that class with the annotation.
        </p>

        <p>
            You can always manually disable getter/setter generation for any field by using the special
            <code>AccessLevel.NONE</code>
            access level. This lets you override the behaviour of a <code>@Getter</code>, <code>@Setter</code> or
            <code>@Data</code> annotation on a class.
        </p>

        <p>
            To put annotations on the generated method, you can use <code>onMethod=@__({@AnnotationsHere})</code>;
            to put annotations on the only parameter of a generated setter method, you can use <code>onParam=@__({@AnnotationsHere})</code>.
            Be careful though! This is an experimental feature. For more details see the documentation on the <a
                href="/features/details/on-x.html">onX</a> feature.
        </p>

        <p>
            <em>NEW in lombok v1.12.0:</em> javadoc on the field will now be copied to generated getters and
            setters. Normally, all text is copied, and <code>@return</code> is <em>moved</em> to the getter, whilst
            <code>@param</code> lines are <em>moved</em> to the setter. Moved means: Deleted from the field's
            javadoc. It is also possible to define unique text for each getter/setter. To do that, you create a
            'section' named <code>GETTER</code> and/or <code>SETTER</code>. A section is a line in your javadoc
            containing 2 or more dashes, then the text 'GETTER' or 'SETTER', followed by 2 or more dashes, and
            nothing else on the line. If you use sections, <code>@return</code> and <code>@param</code> stripping
            for that section is no longer done (move the <code>@return</code> or <code>@param</code> line into the
            section).
        </p>
    </div>
    <@u.comparison />
    <div class="row">
        <h3>Supported configuration keys:</h3>
        <dl>
            <dt><code>lombok.accessors.chain</code> = [<code>true</code> | <code>false</code>] (default: false)</dt>
            <dd>If set to <code>true</code>, generated setters will return <code>this</code> (instead of
                <code>void</code>). An explicitly configured <code>chain</code> parameter of an <a
                        href="/features/details/accessors.html"><code>@Accessors</code></a> annotation takes
                precedence
                over this setting.
            </dd>
            <dt><code>lombok.accessors.fluent</code> = [<code>true</code> | <code>false</code>] (default: false)
            </dt>
            <dd>If set to <code>true</code>, generated getters and setters will not be prefixed with the
                bean-standard '<code>get</code>, <code>is</code> or <code>set</code>; instead, the methods will use
                the same name as the field (minus prefixes). An explicitly configured <code>chain</code> parameter
                of an <a href="/features/details/accessors.html"><code>@Accessors</code></a> annotation takes precedence
                over this setting.
            </dd>
            <dt><code>lombok.accessors.prefix</code> += <em>a field prefix</em> (default: empty list)</dt>
            <dd>This is a list property; entries can be added with the <code>+=</code> operator. Inherited prefixes
                from parent config files can be removed with the <code>-=</code> operator. Lombok will strip any
                matching field prefix from the name of a field in order to determine the name of the getter/setter
                to generate. For example, if <code>m</code> is one of the prefixes listed in this setting, then a
                field named <code>mFoobar</code> will result in a getter named <code>getFoobar()</code>, not
                <code>getMFoobar()</code>.
                An explicitly configured <code>prefix</code> parameter of an <a
                        href="/features/details/accessors.html"><code>@Accessors</code></a> annotation takes precedence
                over this setting.
            </dd>
            <dt><code>lombok.getter.noIsPrefix</code> = [<code>true</code> | <code>false</code>] (default: false)
            </dt>
            <dd>If set to <code>true</code>, getters generated for <code>boolean</code> fields will use the
                <code>get</code>
                prefix instead of the default<code>is</code> prefix, and any generated code that calls getters, such
                as <code>@ToString</code>, will also use <code>get</code> instead of <code>is</code>
            <dt><code>lombok.setter.flagUsage</code> = [<code>warning</code> | <code>error</code>] (default: not
                set)
            </dt>
            <dd>Lombok will flag any usage of <code>@Setter</code> as a warning or error if configured.</dd>
            <dt><code>lombok.getter.flagUsage</code> = [<code>warning</code> | <code>error</code>] (default: not
                set)
            </dt>
            <dd>Lombok will flag any usage of <code>@Getter</code> as a warning or error if configured.</dd>
        </dl>
    </div>
    <div class="row">
        <h3>Small print</h3>

        <div class="smallprint">
            <p>
                For generating the method names, the first character of the field, if it is a lowercase character,
                is title-cased, otherwise, it is left unmodified. Then, get/set/is is prefixed.
            </p>

            <p>
                No method is generated if any method already exists with the same name (case insensitive) and same
                parameter count. For example, <code>getFoo()</code> will not be generated if there's already a
                method <code>getFoo(String... x)</code> even though it is technically possible to make the method.
                This caveat exists to prevent confusion. If the generation of a method is skipped for this reason, a
                warning is emitted instead. Varargs count as 0 to N parameters. You can mark any method with <code>@lombok.experimental.Tolerate</code>
                to hide them from lombok.
            </p>

            <p>
                For <code>boolean</code> fields that start with <code>is</code> immediately followed by a title-case
                letter, nothing is prefixed to generate the getter name.
            </p>

            <p>
                Any variation on <code>boolean</code> will <em>not</em> result in using the <code>is</code> prefix
                instead of the <code>get</code> prefix; for example,
                returning <code>java.lang.Boolean</code> results in a <code>get</code> prefix, not an
                <code>is</code> prefix.
            </p>

            <p>
                Any annotations named <code>@NonNull</code> (case insensitive) on the field are interpreted as: This
                field must not ever hold
                <em>null</em>. Therefore, these annotations result in an explicit null check in the generated
                setter. Also, these
                annotations (as well as any annotation named <code>@Nullable</code> or <code>@CheckForNull</code>)
                are copied to setter parameter and getter method.
            </p>

            <p>
                You can annotate a class with a <code>@Getter</code> or <code>@Setter</code> annotation. Doing so is
                equivalent to annotating all non-static fields
                in that class with that annotation. <code>@Getter</code>/<code>@Setter</code> annotations on fields
                take precedence over the ones on classes.
            </p>

            <p>
                Using the <code>AccessLevel.NONE</code> access level simply generates nothing. It's useful only in
                combination with
                <a href="/features/details/data.html"><code>@Data</code></a> or a class-wide <code>@Getter</code> or
                <code>@Setter</code>.
            </p>

            <p>
                <code>@Getter</code> can also be used on enums. <code>@Setter</code> can't, not for a technical
                reason, but
                for a pragmatic one: Setters on enums are an extremely bad idea.
            </p>
        </div>
    </div>
</div>
</@u.page>
