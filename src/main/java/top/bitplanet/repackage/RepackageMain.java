package top.bitplanet.repackage;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * maven 项目统一修改包名
 */
public class RepackageMain {

    public static void main(String[] args) {
        String base = "/Users/neo/Desktop/bitplanet-devops 2";

        // 原包名
        String originalPackage = "top.bitplanet.devops";
        // 目标包名
        String destPackage = "com.aaa.bbb";



        // 修改包名
        iteratorMavenDir(new File(base),file -> {
            try {
                update(file.getPath(),originalPackage,destPackage);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        // 修改所有文件 引用
        iteratorFile(new File(base),(f) -> {
            // reder
            ReplaceFile.replaceFileStr(f,originalPackage,destPackage);
        });

        // 修改pom文件  artifact
        //
        String originalArtifactId = "<artifactId>credit";
        String destArtifactId = "<artifactId>devops";
        iteratorFile(new File(base),f -> {
            // reder
            ReplaceFile.replaceFileStr(f,originalArtifactId,destArtifactId);
        });
        // 修改模块名前缀
        List<File> allDirs = new ArrayList<>();
        iteratorDir(new File(base),f -> {
           allDirs.add(f);
        });
        // 从后往前迭代
        for (int i = allDirs.size() - 1; i >= 0; i--) {
            File f = allDirs.get(i);
            // 修改项目名
            if (f.getName().startsWith("devops-")) {
                String newName = f.getName().replaceFirst("devops-", "bbb-");
                f.renameTo(new File(f.getParentFile().getPath() + "/" + newName)) ;
                f.delete();
            }
        }

    }

    /**
     * 判断目录是否是一个maven 工程
     * @param dir
     * @return
     */
    private static boolean isMaven(File dir)  {
        if (!dir.isDirectory()) {
            return false;
        }
        // 校验符合 maven 标准文件结构
        // java dir
        File javaBaseDir = new File(dir.getPath() + "/src/main/java");
        // test java 目录
        File javaTestBaseDir = new File(dir.getPath() + "/src/test/java");
        // pom 路径
        File pomFile = new File(dir.getPath() + "/pom.xml");
        if ( !javaBaseDir.isDirectory() || !pomFile.exists() ) {
            return false;
        }
        return true;
    }

    /**
     * 修改
     * @param projectDir
     * @param originalPackage
     * @param destPackage
     * @throws Exception
     */
    public static void update(String projectDir,String originalPackage,String destPackage) throws Exception {

        File projectFile = new File(projectDir);
        if (!projectFile.isDirectory()) {
            throw new Exception("项目路径必须是文件夹!");
        }
        // 校验符合 maven 标准文件结构
        // java 目录
        File javaBaseDir = new File(projectFile.getPath() + "/src/main/java");
        // test java 目录
        File javaTestBaseDir = new File(projectFile.getPath() + "/src/test/java");
        // pom 路径
        File pomFile = new File(projectFile.getPath() + "/pom.xml");
        if ( !javaBaseDir.isDirectory()  || !pomFile.exists() ) {
            throw new Exception("项目必须是标准的Maven 项目结构");
        }
        // 修改目录
        String originDir = originalPackage.replaceAll("\\.", "/");
        String destDir = destPackage.replaceAll("\\.", "/");
        renameTo(javaBaseDir,"/"+originDir,"/" + destDir);
        renameTo(javaTestBaseDir,"/"+originDir,"/" + destDir);
    }

    /**
     * 重命名
     * @param baseDir
     * @param from
     * @param to
     * @return
     */
    private static boolean renameTo(File baseDir,String from,String to ) {
        File file1 = new File(baseDir.getPath() + from);
        File file2 = new File(baseDir.getPath() + to);
        file2.mkdirs();
        boolean result = file1.renameTo(file2);
        if (result) {
            file1.delete();
        }
        return result;
    }

    /**
     * 迭代所有文件,找到是文件的file并执行操作
     * @param file 文件
     * @param fileOperation 文件修改/操作类
     */
    private static void iteratorFile(File file,FileOperation fileOperation) {
        if (!file.isDirectory()) {
            //System.out.println("非文件夹,执行操作" );
            fileOperation.operation(file);
            return;
        }
        File[] files = file.listFiles();
        if (files == null || files.length <=0 ) {
            return;
        }
        for (File f : files) {
            iteratorFile(f,fileOperation);
        }
    }

    /**
     * 迭代所有文件夹,找到是文件夹并执行操作
     * @param file 文件
     * @param fileOperation 文件修改/操作类
     */
    private static void iteratorDir(File file,FileOperation fileOperation) {
        if (!file.isDirectory()) {
            return;
        }
        fileOperation.operation(file);
        File[] files = file.listFiles();
        if (files == null || files.length <=0 ) {
            return;
        }
        for (File f : files) {
            iteratorDir(f,fileOperation);
        }
    }

    /**
     * 迭代所有文件,找到是maven 工程的并操作
     * @param file 文件
     * @param fileOperation 文件修改/操作类
     */
    private static void iteratorMavenDir(File file,FileOperation fileOperation) {
        if (!file.isDirectory()) {
            return;
        }
        if (isMaven(file)) {
            System.out.println("maven工程,执行操作" + file.getName() );
            fileOperation.operation(file);
        }
        File[] files = file.listFiles();
        if (files == null || files.length <=0 ) {
            return;
        }
        for (File f : files) {
            iteratorMavenDir(f,fileOperation);
        }
    }

}
