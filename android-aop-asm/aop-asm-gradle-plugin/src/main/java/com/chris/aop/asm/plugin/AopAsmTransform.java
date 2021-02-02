package com.chris.aop.asm.plugin;

import com.android.build.api.transform.DirectoryInput;
import com.android.build.api.transform.Format;
import com.android.build.api.transform.JarInput;
import com.android.build.api.transform.QualifiedContent;
import com.android.build.api.transform.Transform;
import com.android.build.api.transform.TransformException;
import com.android.build.api.transform.TransformInput;
import com.android.build.api.transform.TransformInvocation;
import com.android.build.api.transform.TransformOutputProvider;
import com.android.build.gradle.internal.pipeline.TransformManager;
import com.android.utils.FileUtils;
import com.chris.aop.asm.plugin.adapter.ClassVisitorAdapter;
import com.chris.aop.asm.plugin.config.TransConstant;

import org.apache.commons.codec.digest.DigestUtils;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class AopAsmTransform extends Transform {
    @Override
    public String getName() {
        return "AopAsmPlugin";
    }

    @Override
    public Set<QualifiedContent.ContentType> getInputTypes() {
        return TransformManager.CONTENT_CLASS;
    }

    @Override
    public Set<? super QualifiedContent.Scope> getScopes() {
        return TransformManager.SCOPE_FULL_PROJECT;
    }

    @Override
    public boolean isIncremental() {
        return true;
    }

    @Override
    public void transform(TransformInvocation transformInvocation) throws TransformException, InterruptedException, IOException {
        // 空方法，删除也可以
        super.transform(transformInvocation);
        System.out.println("【AOP ASM】---------------- begin ----------------");

        TransformOutputProvider provider = transformInvocation.getOutputProvider();
        for (TransformInput input : transformInvocation.getInputs()) {

            for (DirectoryInput di : input.getDirectoryInputs()) {
                doDirectoryInputTransform(di);
                copyQualifiedContent(provider, di, null, Format.DIRECTORY);
            }

            for (JarInput ji : input.getJarInputs()) {
                copyQualifiedContent(provider, ji, getUniqueName(ji.getFile()), Format.JAR);
            }

        }

        System.out.println("【AOP ASM】----------------  end  ----------------");
    }

    /***********************************************************************************************
     * 根据输入的目录，遍历需要插桩的 Class 文件
     ***********************************************************************************************/
    private void doDirectoryInputTransform(DirectoryInput input) {
        List<File> files = new ArrayList<>();
        listFiles(files, input.getFile());

        for (File file : files) {
            try {
                doAsm(file);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void doAsm(File file) throws Exception {
        InputStream is = null;
        FileOutputStream fos = null;

        try {
            is = new FileInputStream(file);
            ClassReader reader = new ClassReader(is);
            ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
            ClassVisitor visitor = new ClassVisitorAdapter(Opcodes.ASM5, writer);
            reader.accept(visitor, ClassReader.EXPAND_FRAMES);

            byte[] code = writer.toByteArray();
            fos = new FileOutputStream(file.getAbsolutePath());
            fos.write(code);

            System.out.println(file.getAbsolutePath() + File.separator + file.getName());

        } finally {
            if (is != null) {
                is.close();
            }
            if (fos != null) {
                fos.close();
            }
        }
    }

    private void listFiles(List<File> list, File file) {
        if (file == null) {
            return;
        }

        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (files == null || files.length == 0) {
                return;
            }

            for (File f : files) {
                listFiles(list, f);
            }
        } else if (needTrack(file.getName())) {
            list.add(file);
        }
    }

    private boolean needTrack(String name) {
        boolean ret = false;
        if (name.endsWith(".class")) {
            int len = TransConstant.CLASS_FILE_IGNORE.length;
            int i = 0;
            while (i < len) {
                if (name.contains(TransConstant.CLASS_FILE_IGNORE[i])) {
                    break;
                }
                i++;
            }
            if (i == len) {
                ret = true;
            }
        }
        return ret;
    }

    /***********************************************************************************************
     * 重名名输出文件,因为可能同名(N个classes.jar),会覆盖
     ***********************************************************************************************/
    private String getUniqueName(File jar) {
        String name = jar.getName();
        String suffix = "";
        if (name.lastIndexOf(".") > 0) {
            suffix = name.substring(name.lastIndexOf("."));
            name = name.substring(0, name.lastIndexOf("."));
        }
        String hexName = DigestUtils.md5Hex(jar.getAbsolutePath());
        return String.format("%s_%s%s", name, hexName, suffix);
    }

    private void copyQualifiedContent(TransformOutputProvider provider, QualifiedContent file, String fileName, Format format) throws IOException {
        boolean useDefaultName = fileName == null;
        File dest = provider.getContentLocation(useDefaultName ? file.getName() : fileName, file.getContentTypes(), file.getScopes(), format);
        if (!dest.exists()) {
            dest.mkdirs();
            dest.createNewFile();
        }

        if (useDefaultName) {
            FileUtils.copyDirectory(file.getFile(), dest);
        } else {
            FileUtils.copyFile(file.getFile(), dest);
        }
    }
}
