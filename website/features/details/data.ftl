<#import "../../freemarker/main-template.ftl" as u>

<@u.page>
<div class="page-header top5">
    <div class="row text-center">
        <div class="header-group">
            <h1>@Data</h1>

            <h3>All together now: A shortcut for <code>@ToString</code>, <code>@EqualsAndHashCode</code>,
                <code>@Getter</code> on all fields, <code>@Setter</code> on all non-final fields, and <code>@RequiredArgsConstructor</code>!
            </h3>
        </div>
    </div>
    <div class="overview">
        <h3>Overview</h3>

        <p>
            <code>@Data</code> is a convenient shortcut annotation that bundles the features of <a
                href="/features/details/to-string.html"><code>@ToString</code></a>,
            <a href="/features/details/equals-and-hashcode.html"><code>@EqualsAndHashCode</code></a>, <a
                href="/features/details/getter-setter.html"><code>@Getter</code>
            / <code>@Setter</code></a> and <a href="/features/details/constructor.html"><code>@RequiredArgsConstructor</code></a>
            together: In other words, <code>@Data</code> generates <em>all</em> the boilerplate that is normally
            associated with simple POJOs (Plain Old Java Objects) and beans: getters for all fields, setters for all
            non-final fields, and appropriate <code>toString</code>, <code>equals</code> and <code>hashCode</code>
            implementations that involve the fields of the class, and a constructor that initializes all final fields,
            as well as all non-final fields with no initializer that have been marked with <code>@NonNull</code>, in
            order to ensure the field is never null.
        </p>

        <p>
            <code>@Data</code> is like having implicit <code>@Getter</code>, <code>@Setter</code>,
            <code>@ToString</code>, <code>@EqualsAndHashCode</code> and <code>@RequiredArgsConstructor</code>
            annotations on the class. However, the parameters of these annotations (such as <code>callSuper</code>,
            <code>includeFieldNames</code> and <code>exclude</code>) cannot be set with <code>@Data</code>. If you need
            to set non-default values for any of these parameters, just add those annotations explicitly;
            <code>@Data</code> is smart enough to defer to those annotations.
        </p>

        <p>
            All generated getters and setters will be <code>public</code>. To override the access level, annotate the
            field or class with an explicit <code>@Setter</code> and/or <code>@Getter</code> annotation. You can also
            use this annotation (by combining it with <code>AccessLevel.NONE</code>) to suppress generating a getter
            and/or setter altogether.
        </p>

        <p>
            All fields marked as <code>transient</code> will not be considered for <code>hashCode</code> and <code>equals</code>.
            All static fields will be skipped entirely (not considered for any of the generated methods, and no
            setter/getter will be made for them).
        </p>

        <p>
            If the class already contains a method with the same name and parameter count as any method that would
            normally be generated, that method is not generated, and no warning or error is emitted. For example, if you
            already have a method with signature <code>equals(AnyType param)</code>, no <code>equals</code> method will
            be generated, even though technically it might be an entirely different method due to having different
            parameter types. The same rule applies to the constructor (any explicit constructor will prevent
            <code>@Data</code> from generating one), as well as <code>toString</code>, <code>equals</code>, and all
            getters and setters. You can mark any constructor or method with <code>@lombok.experimental.Tolerate</code>
            to hide them from lombok.
        </p>

        <p>
            <code>@Data</code> can handle generics parameters for fields just fine. In order to reduce the boilerplate
            when constructing objects for classes with
            generics, you can use the <code>staticConstructor</code> parameter to generate a private constructor, as
            well as a static method that returns a new instance. This way, javac will infer the variable name. Thus, by
            declaring like so: <code>@Data(staticConstructor="of") class Foo&lt;T&gt; { private T x;}</code> you can
            create new instances of <code>Foo</code> by writing: <code>Foo.of(5);</code> instead of having to write:
            <code>new Foo&lt;Integer&gt;(5);</code>.
        </p>
    </div>
    <@u.comparison />
    <div class="row">
        <h3>Supported configuration keys:</h3>
        <dl>
            <dt><code>lombok.data.flagUsage</code> = [<code>warning</code> | <code>error</code>] (default: not set)</dt>
            <dd>Lombok will flag any usage of <code>@Data</code> as a warning or error if configured.</dd>
        </dl>
    </div>
    <div class="overview">
        <h3>Small print</h3>

        <div class="smallprint">
            <p>See the small print of <a href="/features/details/to-string.html"><code>@ToString</code></a>, <a
                    href="/features/details/equals-and-hashcode.html"><code>@EqualsAndHashCode</code></a>,
                <a href="/features/details/getter-setter.html"><code>@Getter / @Setter</code></a> and <a
                        href="/features/details/contructor.html">@RequiredArgsConstructor</a>.
            </p>

            <p>
                Any annotations named <code>@NonNull</code> (case insensitive) on a field are interpreted as: This field
                must not ever hold
                <em>null</em>. Therefore, these annotations result in an explicit null check in the generated
                constructor for the provided field. Also, these
                annotations (as well as any annotation named <code>@Nullable</code>) are copied to the constructor
                parameter, in both the true constructor and
                any static constructor. The same principle applies to generated getters and setters (see the
                documentation for <a href="/features/details/getter-setter.html">@Getter / @Setter</a>)
            </p>

            <p>
                By default, any variables that start with a $ symbol are excluded automatically.
                You can include them by specifying an explicit annotation (<code>@Getter</code> or
                <code>@ToString</code>, for example) and using the 'of' parameter.
            </p>
        </div>
    </div>
</div>
</@u.page>
