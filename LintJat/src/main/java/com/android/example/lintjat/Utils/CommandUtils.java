package com.android.example.lintjat.Utils;

import com.android.tools.lint.detector.api.Project;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class CommandUtils {


    public static void readStream(InputStream inputStream, IReadLineCallback callback) {
        InputStreamReader isr = null;
        BufferedReader reader = null;
        String str;
        try {
            isr = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
            reader = new BufferedReader(isr);
            while ((str = reader.readLine()) != null) {
                if (!callback.onRead(str)) {
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            CommandUtils.closeStream(reader, isr);
        }
    }

    public static void runCommand(String s, IReadLineCallback callback) {
        InputStream inputStream = null;
        try {
            inputStream = exec(s);
            readStream(inputStream, callback);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            CommandUtils.closeStream(inputStream);
        }

    }

    public static InputStream exec(String cmdStr) throws InterruptedException, IOException {
        Process process = Runtime.getRuntime().exec(cmdStr);
        process.waitFor();
        int exitValue = process.exitValue();
        if (exitValue != 0) {
            //执行错误
        }
        return process.getInputStream();
    }

    public static void closeStream(Closeable... closeables) {
        if (closeables == null) {
            return;
        }
        for (Closeable closeable : closeables) {
            if (closeable != null) {
                try {
                    closeable.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    private final static String CHANGED_COM = "git status %1s -s --no-renames";
    private final static String VERSION_COM = "git log --pretty=format:%h -1";
    private final static String VERSION_ChANGE_COM = "git diff --name-only %1s %2s";
    /**
     * 调用的是 git status指令
     * 这个仅仅是获取没有commit的文件信息，可能已经add但是没有commit 也有可能既没有add也没有commit
     * @return
     */
    public static Collection<FileStatus> getFileStatusList(Project projectDir) {
        List<FileStatus> list = new ArrayList<>();
        CommandUtils.runCommand(String.format(CHANGED_COM, projectDir.getDir().getAbsolutePath()), new IReadLineCallback() {
            @Override
            public boolean onRead(String line) {
                if (line.length() < 3) {
                    return true;
                }
                FileStatus fileStatus = new FileStatus();
                String type = line.substring(0, 2).trim();
                String path = line.substring(2).trim().split(" ")[0];
                fileStatus.setPath(path);
                fileStatus.setStatus(getIntStatus(type));
                list.add(fileStatus);
                return false;
            }
        });
        return list;
    }


    /**最新的版本号
     * @return
     */
    public static String getVersion() {
        StringBuilder builder = new StringBuilder();
        CommandUtils.runCommand(VERSION_COM, line -> {
            builder.append(line.trim());
            return true;
        });
        return builder.toString();
    }
    /**获取两个版本之间的不同，调用的是git diff指令，
     * @return
     */
    public static Collection<File> getVersionFileList(Project projectDir) {
        return   getVersionFileList("HEAD^","HEAD",projectDir);
    }
    /**获取两个版本之间的不同，调用的是git diff指令，
     *
     * @param startVersion
     * @param endVersion
     * @return
     */
    public static Collection<File> getVersionFileList(String startVersion, String endVersion,Project projectDir) {
        List<File> list = new ArrayList<>();
        int length = projectDir.getName().length();
        CommandUtils.runCommand(String.format(VERSION_ChANGE_COM, startVersion, endVersion), line -> {
            File file = new File(projectDir.getDir(), line.substring(length).trim());
            if (file.isFile() && file.exists()) {
                list.add(file);
            }
            return true;
        });
        return list;
    }

    public interface IReadLineCallback {
        /**
         * @return false:打断读取 true:继续读取
         */
        boolean onRead(String line);
    }

    /**
     * 将SVN 或 GIT中的文件状态表示方式统一
     */
    public  static FileStatus.FStatus getIntStatus(String s) {
        switch (s) {
            case "D":
            case "!":
            case "C":
                return FileStatus.FStatus.DELETE;
            case "A":
            case "?":
                return FileStatus.FStatus.ADD;
            case "M":
            case "??":
            case "R":
            default:
                return FileStatus.FStatus.MODIFY;
        }
    }
}
