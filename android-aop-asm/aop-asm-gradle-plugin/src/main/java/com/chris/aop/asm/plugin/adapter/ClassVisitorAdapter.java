package com.chris.aop.asm.plugin.adapter;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;

/***************************************************************************************************
 * Author: Chris.yang 
 * 1/29/21
 ***************************************************************************************************/
public class ClassVisitorAdapter extends ClassVisitor {
    private String clazzName;

    public ClassVisitorAdapter(int api, ClassVisitor classVisitor) {
        super(api, classVisitor);
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        super.visit(version, access, name, signature, superName, interfaces);
        this.clazzName = name;
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
        MethodVisitor methodVisitor = cv.visitMethod(access, name, descriptor, signature, exceptions);
        return new MethodAdviceAdapter(api, methodVisitor, access, name, descriptor, this.clazzName);
    }
}
