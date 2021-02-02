package com.chris.aop.asm.plugin.adapter;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.commons.AdviceAdapter;

/***************************************************************************************************
 * Author: Chris.yang 
 * 1/29/21
 ***************************************************************************************************/
public class MethodAdviceAdapter extends AdviceAdapter {
    private String qualifiedName;
    private String clazzName;
    private String methodName;
    private int access;
    private String desc;

    protected MethodAdviceAdapter(int api, MethodVisitor methodVisitor, int access, String name, String descriptor, String clazzName) {
        super(api, methodVisitor, access, name, descriptor);
        this.qualifiedName = clazzName.replaceAll("/", ".");
        this.clazzName = clazzName;
        this.methodName = name;
        this.access = access;
        this.desc = descriptor;
    }

    @Override
    protected void onMethodEnter() {
        enter();
    }

    @Override
    protected void onMethodExit(int opcode) {
        exit(opcode);
    }

    private void enter() {
        // 如果是构造函数则跳过
        if (methodName.equals("<init>")) {
            return;
        }

        mv.visitLdcInsn("Chris");
        mv.visitTypeInsn(NEW, "java/lang/StringBuilder");
        mv.visitInsn(DUP);
        mv.visitMethodInsn(INVOKESPECIAL, "java/lang/StringBuilder", "<init>", "()V", false);
        mv.visitLdcInsn(qualifiedName + ".onCreate.onEnter timestamp = ");
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false);
        mv.visitMethodInsn(INVOKESTATIC, "java/lang/System", "currentTimeMillis", "()J", false);
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(J)Ljava/lang/StringBuilder;", false);
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "toString", "()Ljava/lang/String;", false);
        mv.visitMethodInsn(INVOKESTATIC, "android/util/Log", "e", "(Ljava/lang/String;Ljava/lang/String;)I", false);
        mv.visitInsn(POP);

    }

    private void exit(int opcode) {
        if (methodName.equals("<init>")) {
            return;
        }

        mv.visitLdcInsn("Chris");
        mv.visitTypeInsn(NEW, "java/lang/StringBuilder");
        mv.visitInsn(DUP);
        mv.visitMethodInsn(INVOKESPECIAL, "java/lang/StringBuilder", "<init>", "()V", false);
        mv.visitLdcInsn(qualifiedName+ ".onCreate.onExit timestamp = ");
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false);
        mv.visitMethodInsn(INVOKESTATIC, "java/lang/System", "currentTimeMillis", "()J", false);
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(J)Ljava/lang/StringBuilder;", false);
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "toString", "()Ljava/lang/String;", false);
        mv.visitMethodInsn(INVOKESTATIC, "android/util/Log", "e", "(Ljava/lang/String;Ljava/lang/String;)I", false);
        mv.visitInsn(POP);
    }
}
