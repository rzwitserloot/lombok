/*
 * Copyright (C) 2009-2021 The Project Lombok Authors.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package lombok.javac.handlers;

import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.util.List;
import lombok.ConfigurationKeys;
import lombok.core.AST;
import lombok.experimental.Locked;
import lombok.javac.JavacNode;
import lombok.javac.JavacTreeMaker;

import java.lang.annotation.Annotation;

import static com.sun.tools.javac.tree.JCTree.*;
import static lombok.core.handlers.HandlerUtil.handleFlagUsage;
import static lombok.javac.handlers.JavacHandlerUtil.*;

/**
 * Container for static utility methods used by the Locked[.Read/Write] annotations.
 */
public final class HandleLockedUtil {
	private static final String INSTANCE_LOCK_NAME = "lock";
	private static final String STATIC_LOCK_NAME = "LOCK";
	private static final List<JCExpression> NIL_EXPRESSION = List.nil();

	private HandleLockedUtil() {
		//Prevent instantiation
	}

	/**
	 * See {@link #handle(String, JCAnnotation, JavacNode, Class, String, String[], String)} for
	 * {@code lockableMethodName = null}.
	 */
	public static <T extends Annotation> void handle(String annotationValue, JCTree.JCAnnotation ast,
													 JavacNode annotationNode, Class<T> annotationClass,
													 String annotationName, String[] lockClass) {
		handle(annotationValue, ast, annotationNode, annotationClass, annotationName, lockClass, null);
	}

	/**
	 * Called when an annotation is found that is likely to match {@link Locked}, {@link Locked.Read}, or
	 * {@link Locked.Write}.
	 *
	 * Be aware that you'll be called for ANY annotation node in the source that looks like a match. There is,
	 * for example, no guarantee that the annotation node belongs to a method, even if you set your
	 * TargetType in the annotation to methods only.
	 *
	 * @param annotationValue The value of the annotation. This will be the name of the object used for locking.
	 * @param ast The javac AST node representing the annotation.
	 * @param annotationNode The Lombok AST wrapper around the 'ast' parameter. You can use this object
	 * to travel back up the chain (something javac AST can't do) to the parent of the annotation, as well
	 * as access useful methods such as generating warnings or errors focused on the annotation.
	 * @param annotationClass The specific annotation class. This should be one of {@code Locked}, {@code Locked.Read},
	 * or {@code Locked.Write}.
	 * @param annotationName The name of the annotation to use when referencing it in errors.
	 * @param lockClass The type of lock to use.
	 * See {@link JavacHandlerUtil#chainDots(JavacNode, String, String, String...)}.
	 * @param lockableMethodName The name of the method in the {@code lockClass} that returns a
	 * {@link java.util.concurrent.locks.Lock} object. When this is {@code null}, it is assumed that {@code lockClass}
	 * itself can be locked/unlocked.
	 * @param <T> The annotation type.
	 */
	public static <T extends Annotation> void handle(String annotationValue, JCTree.JCAnnotation ast,
													 JavacNode annotationNode, Class<T> annotationClass,
													 String annotationName, String[] lockClass,
													 String lockableMethodName) {
		handleFlagUsage(annotationNode, ConfigurationKeys.LOCKED_FLAG_USAGE, annotationName);

		if (inNetbeansEditor(annotationNode)) return;
		deleteAnnotationIfNeccessary(annotationNode, annotationClass);

		JavacNode methodNode = annotationNode.up();
		if (methodNode == null || methodNode.getKind() != AST.Kind.METHOD ||
			!(methodNode.get() instanceof JCMethodDecl)) {
			annotationNode.addError(annotationName + " is legal only on methods.");
			return;
		}

		JCMethodDecl method = (JCMethodDecl) methodNode.get();
		if ((method.mods.flags & Flags.ABSTRACT) != 0) {
			annotationNode.addError(annotationName + " is legal only on concrete methods.");
			return;
		}

		JavacNode typeNode = upToTypeNode(annotationNode);
		if (!isClassOrEnum(typeNode)) {
			annotationNode.addError(annotationName + " is legal only on methods in classes and enums.");
			return;
		}

		boolean[] isStatic = new boolean[]{(method.mods.flags & Flags.STATIC) != 0};
		String lockName = annotationValue;

		if (lockName.length() == 0) {
			lockName = isStatic[0] ? STATIC_LOCK_NAME : INSTANCE_LOCK_NAME;
		}

		JavacTreeMaker maker = methodNode.getTreeMaker().at(ast.pos);

		MemberExistsResult exists = MemberExistsResult.NOT_EXISTS;

		JCExpression lockType = chainDots(methodNode, lockClass);

		if (typeNode != null && typeNode.get() instanceof JCClassDecl) {
			for (JCTree def : ((JCClassDecl) typeNode.get()).defs) {
				if (def instanceof JCVariableDecl) {
					if (((JCVariableDecl) def).name.contentEquals(lockName)) {
						JCVariableDecl varDeclDef = (JCVariableDecl) def;
						exists = getGeneratedBy(varDeclDef) == null ? MemberExistsResult.EXISTS_BY_USER :
								 MemberExistsResult.EXISTS_BY_LOMBOK;
						boolean st = ((varDeclDef.mods.flags) & Flags.STATIC) != 0;

						if (isStatic[0] != st && exists == MemberExistsResult.EXISTS_BY_LOMBOK) {
							annotationNode.addError("The generated field " + lockName +
														" does not match the static status of this method");
							return;
						}
						isStatic[0] = st;

						if (exists == MemberExistsResult.EXISTS_BY_LOMBOK &&
							!lockType.toString().equals(varDeclDef.vartype.toString())) {
							annotationNode.addError("Expected field " + lockName + " to be of type " + lockType +
														" but got type " + varDeclDef.vartype + "!" +
														" Did you mix @Locked with @Locked.Read/Write on the same generated field?");
							return;
						}
					}
				}
			}
		}

		if (exists == MemberExistsResult.NOT_EXISTS) {
			JCNewClass lockInstance = maker.NewClass(null, NIL_EXPRESSION, lockType, NIL_EXPRESSION, null);
			JCVariableDecl newLockField = recursiveSetGeneratedBy(maker.VarDef(
				maker.Modifiers(Flags.PRIVATE | Flags.FINAL | (isStatic[0] ? Flags.STATIC : 0)),
				methodNode.toName(lockName), lockType, lockInstance
			), annotationNode);

			injectFieldAndMarkGenerated(methodNode.up(), newLockField);
		}

		if (method.body == null || typeNode == null) return;

		JCExpression lockNode;
		if (isStatic[0]) {
			lockNode = namePlusTypeParamsToTypeReference(maker, typeNode, methodNode.toName(lockName), false,
														 List.<JCTypeParameter>nil());
		} else {
			lockNode = maker.Select(maker.Ident(methodNode.toName("this")), methodNode.toName(lockName));
		}

		recursiveSetGeneratedBy(lockNode, annotationNode);

		final JCExpression lockable;
		if (lockableMethodName == null) {
			lockable = lockNode;
		} else {
			lockable = maker.Apply(NIL_EXPRESSION,
								   maker.Select(lockNode, methodNode.toName(lockableMethodName)),
								   NIL_EXPRESSION);
		}

		JCExpressionStatement acquireLock =
			maker.Exec(maker.Apply(NIL_EXPRESSION, maker.Select(lockable, typeNode.toName("lock")), NIL_EXPRESSION));
		JCExpressionStatement releaseLock =
			maker.Exec(maker.Apply(NIL_EXPRESSION, maker.Select(lockable, typeNode.toName("unlock")), NIL_EXPRESSION));

		JCTry tryBlock = recursiveSetGeneratedBy(maker.Try(maker.Block(0L, method.body.stats),
														   List.<JCCatch>nil(),
														   maker.Block(0L, List.<JCStatement>of(releaseLock))),
												 annotationNode);

		method.body.stats = List.<JCStatement>of(tryBlock)
								.prepend(recursiveSetGeneratedBy(acquireLock, annotationNode));
		methodNode.rebuild();
	}
}
