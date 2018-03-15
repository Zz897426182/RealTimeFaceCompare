package com.hzgc.collect.expand.merge;

import com.hzgc.collect.expand.log.LogEvent;
import com.hzgc.collect.expand.util.JSONHelper;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.regex.Pattern;


/**
 * merge模块与文件读写相关的工具类（马燊偲）
 */
public class MergeUtil {

    private Logger LOG = Logger.getLogger(MergeUtil.class);
    //系统换行符
    private String newLine = System.getProperty("line.separator");
    private static final String SUFFIX = ".log";
    private static final String ERR_FILE_NAME = "error.log";
    //用于生成success目录下备份日志目录的日期格式
    private static final String SUC_DATE_FORMAT = "yyyy-MM";
    //用于生成错误日志文件名随机数的日期格式
    private static final String ERR_DATE_FORMAT = "yyyy-MM-dd-HHmmSSS-";

    /*
      使用Files工具类中的walkFileTree()方法实现对目录下的所有文件进行遍历。
      这个方法需要一个Path和一个FileVisitor参数。
      其中Path是要遍历的路径，而FileVisitor则可以看成的一个文件访问器，它主要提供了四个方法。
      这四个方法返回的都是FileVisitResult对象，它是一个枚举类，代表的是返回之后的一些后续的操作。

      FileVisitResult主要包含四个常见的操作：
      1、FileVisitResult.CONTINUE 继续遍历
      2、FileVisitResult.TERMINATE 中止访问
      3、FileVisitResult.SKIP_SIBLINGS 不访问同级的文件或目录
      4、FileVisitResult.SKIP_SUBTREE 不访问子目录

      通过创建SimpleFileVisitor对象来对文件进行遍历即可，它是FileVisitor的实现类，这样可以有选择的重写指定的方法。
     */

    /**
     * NIO扫描得到某个目录下的所有文件的绝对路径的List
     *
     * @param path 需扫描的根目录
     * @return 该根目录下所有文件的FileList
     */
    List<String> listAllFileAbsPath(String path) {
        List<String> allFilePath = new ArrayList<>();
        try {
            if (path != null && !Objects.equals(path, "")) {
                //若传入的参数是一个目录
                if (Files.isDirectory(Paths.get(path))) {
                    //用NIO对path目录下的文件进行递归遍历
                    Files.walkFileTree(Paths.get(path), new SimpleFileVisitor<Path>() {
                        //访问文件时触发该方法。
                        @Override
                        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                            if (file.toString().contains(SUFFIX)) {
                                allFilePath.add(file.toString());
                            }
                            return FileVisitResult.CONTINUE;
                        }
                    });
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return allFilePath;
    }


    /**
     * NIO扫描得到process目录下所有错误日志/process/p-N/error/error.log绝对路径的List
     *
     * @param path 需扫描的根目录
     * @return 该目录下所有错误日志/process/p-N/error/error.log绝对路径的List
     */
    List<String> listAllErrorLogAbsPath(String path) {
        List<String> allErrorFilePath = new ArrayList<>();
        try {
            if (path != null && !Objects.equals(path, "")) {
                //若传入的参数是一个目录
                if (Files.isDirectory(Paths.get(path))) {
                    //用NIO对path目录下的文件进行递归遍历
                    Files.walkFileTree(Paths.get(path), new SimpleFileVisitor<Path>() {
                        //访问文件时触发该方法。
                        @Override
                        public FileVisitResult visitFile(Path path, BasicFileAttributes attrs) throws IOException {
                            //将所有process下所有error日志添加到List
                            if (path.toString().contains("error.log")) {
                                allErrorFilePath.add(path.toString());
                            }
                            return FileVisitResult.CONTINUE;
                        }
                    });
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return allErrorFilePath;
    }

    /**
     * NIO扫描得到process目录下除了error/error.log以外的所有log日志的List
     * （包括0000.log、包括最大的日志文件）
     *
     * @param path 需扫描的根目录
     * @return 该根目录除了0000.log以及最大的日志文件外所有文件的FileList
     */
    List<String> listAllProcessLogAbsPath(String path) {
        List<String> allProcessLogPath = new ArrayList<>();
        try {
            if (path != null && !Objects.equals(path, "")) {
                //若传入的参数是一个目录
                if (Files.isDirectory(Paths.get(path))) {
                    //用NIO对path目录下的文件进行递归遍历
                    Files.walkFileTree(Paths.get(path), new SimpleFileVisitor<Path>() {
                        //访问文件时触发该方法。
                        public FileVisitResult visitFile(Path path, BasicFileAttributes attrs) throws IOException {
                            //将所有process下所有除了error.log的日志添加到List
                            if (!path.toString().contains("error")) {
                                allProcessLogPath.add(path.toString());
                            }
                            return FileVisitResult.CONTINUE;
                        }
                    });
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return allProcessLogPath;
    }

    /**
     * NIO扫描得到process目录下的除了0000.log、除了最大的日志文件、除了error/error.log
     * 之外，所有文件的绝对路径的List
     *
     * @param path 需扫描的根目录
     * @return 该根目录除了0000.log以及最大的日志文件外所有文件的FileList
     */
    List<String> listAllBackupLogAbsPath(String path, String writingLogFile) {
        //writingLogFile：遍历时需要跳过的0000000.log文件
        List<String> allFileOfPath = new ArrayList<>();
        try {
            if (path != null && !Objects.equals(path, "")) {
                //若传入的参数是一个目录
                if (Files.isDirectory(Paths.get(path))) {
                    //用NIO对path目录下的文件进行递归遍历
                    Files.walkFileTree(Paths.get(path), new SimpleFileVisitor<Path>() {
                        // 访问目录时触发该方法
                        // 目录结构为：./data/process/r-0/000000000001.log
                        // 传入目录为 ./data/process/这一级
                        public FileVisitResult postVisitDirectory(Path path, IOException exc) throws IOException {
                            //若目录包含“-”，则一定是/data/receive/这一级目录
                            if (path.toString().contains("-") && !path.toString().contains("error")) {
                                //获取/data/receive/r-0/这一级目录下面的所有日志文件和文件夹，
                                // 不包括0000.log和最大值的日志文件，不包括error日志文件夹
                                File[] allFiles = path.toFile().listFiles();

                                //以<文件名数值：文件绝对路径>的k-v方式放入map
                                //遍历获取目录下最大值的文件Key：2000 -> value：../r-0/2000.log
                                Map<Integer, String> fileNameMap = new HashMap<>();
                                if (allFiles != null) {
                                    for (File allFile : allFiles) {
                                        //文件名，不含后缀
                                        String filename = allFile.getName().replace(SUFFIX, "");
                                        Pattern pattern = Pattern.compile("^[-\\+]?[\\d]*$");
                                        //判断文件名是否是整数，并排除读到error/error.log的可能性
                                        if (pattern.matcher(filename).matches() && !filename.contains("error")) {
                                            int fileName = Integer.parseInt(filename);
                                            fileNameMap.put(fileName, allFile.toString());
                                        }
                                    }
                                    Set<Integer> fileNameSet = fileNameMap.keySet();
                                    //当路径下除了000.log和最大文件还有其他文件时，才去获取这些其他文件；否则返回的是空的list
                                    if (fileNameSet.size() > 1){
                                        //获取最大的文件名对应的key
                                        int maxFile = Collections.max(fileNameMap.keySet());
                                        //从需要遍历的MAP中，删除这个最大的文件，和0000000.log文件
                                        fileNameMap.remove(maxFile);
                                        fileNameMap.remove(Integer.parseInt(writingLogFile.replace(SUFFIX, "")));
                                        //遍历map，将除去这两个文件后的所有文件，放入list
                                        for (Map.Entry<Integer, String> entry : fileNameMap.entrySet()) {
                                            allFileOfPath.add(entry.getValue());
                                        }
                                    }
                                }
                            }
                            return FileVisitResult.CONTINUE;
                        }
                    });
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return allFileOfPath;
    }


    /**
     * NIO读取两个文件，把两个文件的每行内容导入到一个List
     *
     * @param filePaths 两个文件的路径
     * @return 包含两个文件中所有内容的List
     */
    List<String> getAllContentFromFile(String... filePaths) {
        //最终添加到的List
        List<String> allContentList = new ArrayList<>();
        //记录入参中符合文件路径格式的参数个数（入参可能为文件夹路径）
        int count = 0;
        if (filePaths != null) {
            for (String filePath : filePaths) {
                //入参均不为空，再执行以下操作
                if (filePath != null && !Objects.equals(filePath, "")) {
                    File file = new File(filePath);
                    //判断入参类型是文件的个数
                    if (file.isFile()) {
                        count++;
                    }
                }
            }
            try {
                //若入参都是文件的绝对路径
                if (count == filePaths.length) {
                    for (String filePath : filePaths) {
                        //先将每个文件的内容，导入到各自的contentList中
                        List<String> contentList = Files.readAllLines(Paths.get(filePath),  StandardCharsets.UTF_8);
                        //再导入到最终的allContentList中
                        allContentList.addAll(contentList);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return allContentList;
    }

    /**
     * 循环删除文件或者整个目录
     *
     * @param path 文件或目录
     */

    public void deleteFile(String path) {
        if (path != null && !Objects.equals(path, "")) {
            deleteFile(new File(path));
        }
    }

    private void deleteFile(File file) {
        if (file.exists() && file.isFile()) {
            file.delete();
        }
        if (file.exists() && file.isDirectory()) {
            File[] files = file.listFiles();
            if (files == null || files.length == 0) {
                file.delete();
            } else {
                for (File file1 : files) {
                    if (file1.isDirectory()) {
                        deleteFile(file1); //递归
                        continue; //跳出本次循环，继续下次循环
                    }
                    file1.delete();
                }
            }
        }
    }


    /**
     * 根据文件路径判断文件是否存在
     *
     * @param filePath 需判断的文件所在路径
     * @return 文件是否存在
     */
    boolean isFileExist(String filePath) {
        //若输入为空
        if (filePath == null || Objects.equals(filePath, "")) {
            return false;
        } else { //输入不为空，文件为目录
            File file = new File(filePath);
            //输入为目录或文件不存在
            return file.isFile() && file.exists();
        }
    }

    /**
     * 以追加的方式一行行写日志（参照AbstractLogWrite类中的action方法，但多了一个保存目录的入参）
     * 包括：
     * 1、把processFile，写到/data/process/p-0/...指定的文件中
     * 2、把errProFiles中的每一条数据，写到/data/merge/error目录下指定文件中
     *
     * @param event         每一条数据（LogEvent格式）
     * @param mergeFilePath 要写入的文件绝对路径
     */
    void writeMergeFile(LogEvent event, String mergeFilePath) {
        if (mergeFilePath != null && !Objects.equals(mergeFilePath, "")) {
            FileWriter fw = null;
            File mergeFile = new File(mergeFilePath);
            //获取入参文件绝对路径的父目录
            File folderPath = mergeFile.getParentFile();
            try {
                //若文件夹路径不存在，先创建
                if (!folderPath.exists()) {
                    folderPath.mkdirs();
                }
                //若日志文件不存在，先创建
                if (!mergeFile.exists()) {
                    mergeFile.createNewFile();
                }
                //用File对象构造FileWriter，如果第二个参数为true，表示以追加的方式写数据，从文件尾部开始写起
                fw = new FileWriter(mergeFilePath, true);
                fw.write(JSONHelper.toJson(event));
                fw.write(newLine);
                fw.flush();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (fw != null) {
                        fw.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    /**
     * NIO将源文件移动到目标文件
     *
     * @param sourceFile      源文件绝对路径
     * @param destinationFile 目标文件绝对路径
     */
    void moveFile(String sourceFile, String destinationFile) {
        if (sourceFile != null && destinationFile != null
                && !Objects.equals(sourceFile, "")
                && !Objects.equals(destinationFile, "")) {
            //判断源文件是否存在、是否是文件
            if (new File(sourceFile).exists() && new File(sourceFile).isFile()) {
                try {
                    //根据目标文件路径：/opt/logdata/process/p-0/000000000001.log
                    //获取目标文件父目录：/opt/logdata/process/p-0/
                    File destinationFolder = new File(destinationFile).getParentFile();
                    //若所在文件夹路径不存在，先创建
                    if (!destinationFolder.exists()) {
                        destinationFolder.mkdirs();
                    }
                    //移动文件。REPLACE_EXISTING: 如果目标文件存在，则替换。如果不存在，则移动。
                    Files.move(Paths.get(sourceFile), Paths.get(destinationFile),
                            StandardCopyOption.REPLACE_EXISTING);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * NIO将源文件拷贝到目标文件
     *
     * @param sourceFile      源文件
     * @param destinationFile 目标文件
     */
    void copyFile(String sourceFile, String destinationFile) {
        if (sourceFile != null && destinationFile != null
                && !Objects.equals(sourceFile, "")
                && !Objects.equals(destinationFile, "")) {
            //判断源文件是否存在、是否是文件
            if (new File(sourceFile).exists() && new File(sourceFile).isFile()) {
                try {
                    //根据目标文件路径：/opt/logdata/process/p-0/000000000001.log
                    //获取目标文件父目录：/opt/logdata/process/p-0/
                    File destinationFolder = new File(destinationFile).getParentFile();
                    //若所在文件夹路径不存在，先创建
                    if (!destinationFolder.exists()) {
                        destinationFolder.mkdirs();
                    }
                    //拷贝文件。REPLACE_EXISTING: 如果目标文件存在，则替换。如果不存在，则移动。
                    Files.copy(Paths.get(sourceFile), Paths.get(destinationFile),
                            StandardCopyOption.COPY_ATTRIBUTES);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 根据process日志路径，获得对应的receive的日志路径
     *
     * @param processFilePath process日志绝对路径
     * @return 对应的receive的日志绝对路径
     */
    String getRecFilePathFromProFile(String processFilePath) {
        String receiveFilePath = "";
        if (processFilePath != null && !Objects.equals(processFilePath, "")) {
            File file = new File(processFilePath);
            if (file.isFile() && processFilePath.contains("process" + File.separator + "process-")) {
                receiveFilePath = processFilePath.replace("process" + File.separator + "process-",
                        "receive" + File.separator + "receive-");
            }
        }
        return receiveFilePath;
    }

    /**
     * 根据process日志路径，获得对应的error日志路径
     * 例如：./ftp/data/process/p-0/000000000001.log
     * - >/ftp/data/process/p-0/error/error.log
     *
     * @param processFilePath process日志绝对路径
     * @return 对应的error日志绝对路径
     */
    String getErrFilePathFromProFile(String processFilePath) {
        String errorFilePath = "";
        if (processFilePath != null && !Objects.equals(processFilePath, "")) {
            File file = new File(processFilePath);
            if (file.isFile() && processFilePath.contains("process" + File.separator + "process-")) {
                //获取process日志路径的父目录：/ftp/data/process/p-0/
                String parentFolder = file.getParent();
                errorFilePath = parentFolder + File.separator + "error.log";
            }
        }
        return errorFilePath;
    }

    /**
     * 根据receiveFile或processFile的文件路径，获取对应merge下的文件路径。例如：
     * /ftp/data/receive/r-0/000000000001.log ->
     * /ftp/merge/receive/r-0/000000000001.log
     * <p>
     * 如果是error日志，需要得到这样的路径：
     * /ftp/data/process/p-0/error/error.log ->
     * /ftp/merge/process/p-0/error/error.log
     *
     * @param file receiveFile或processFile的文件路径
     * @return 对应的merge下的文件路径
     */
    String getMergeFilePath(String file) {
        String mergeFilePath = "";
        if (file != null && !Objects.equals(file, "")) {
            if (new File(file).isFile()) {
                String subStrEnd;
                //如果是receive日志
                if (file.contains("receive") && !file.contains("error")) { //如果是receive日志
                    subStrEnd = file.substring(file.indexOf(File.separator + "receive"));
                } else if (file.contains("process") && !file.contains("error")) { //如果是process日志
                    subStrEnd = file.substring(file.indexOf(File.separator + "process"));
                } else { //如果是error日志
                    subStrEnd = file.substring(file.indexOf(File.separator + "error"));
                }
                String subStrStart = file.replace(file.substring(file.lastIndexOf(File.separator + "data")), "");
                String subStrMerge = File.separator + "merge";
                mergeFilePath = subStrStart + subStrMerge + subStrEnd;
                // 如果是错误日志，获取其对应的merge目录下路径时，需要重命名。
                if (file.contains("error")) {
                    // 当错误日志重名时，重新随机重命名。
                    do {
                        mergeFilePath = renameErrorLog(file, mergeFilePath);
                    } while (new File(mergeFilePath).exists());
                }
            }
        }
        return mergeFilePath;
    }


    /**
     * 根据./data/process或./data/receive目录下的文件路径，获取对应的success目录下的文件路径。
     * 需要根据data文件最后的修改时间，将文件移动到success对应的“年-月”文件夹下。例如：
     * /opt/RealTimeFaceCompare/ftp/data/process/p-0/000000001.log  ->
     * /opt/RealTimeFaceCompare/ftp/success/process/201802/p-0/000000001.log
     * 获取错误日志对应的success目录下路径时，需要对其重命名。
     *
     * @param datafile /data目录下文件绝对路径
     * @return data目录下文件对应的在success目录下的绝对路径
     */
    String getSuccessFilePath(String datafile) {
        String successFilePath = "";
        if (datafile != null && !Objects.equals(datafile, "")) {
            File file = new File(datafile);
            if (file.isFile() && datafile.contains("data" + File.separator)) {
                //替换data文件路径中的“data”字串为success，得到初步的没有日期的目录：
                //即/opt/RealTimeFaceCompare/ftp/success/process/p-0/000000001.log
                String tmpString = datafile.replace("data", "success");

                String substringEnd = "";
                //获取路径子串：p-0/000000001.log或r-0/000000001.log
                if (datafile.contains("process")) {
                    substringEnd = tmpString.substring(tmpString.indexOf("process-"));
                } else if (datafile.contains("receive")) {
                    substringEnd = tmpString.substring(tmpString.indexOf("receive-"));
                }
                //获取路径子串：/opt/RealTimeFaceCompare/ftp/success/process/
                String substringStart = tmpString.replace(substringEnd, "");
                //获取日期路径子串：
                String substringDate = getFileLastModified(datafile, SUC_DATE_FORMAT) + File.separator;
                successFilePath = substringStart + substringDate + substringEnd;

                // 如果是错误日志，获取其对应的success目录下路径时，需要对error.log重命名。
                if (datafile.contains("error")) {
                    // 当错误日志重名时，就重新随机重命名。
                    do {
                        successFilePath = renameErrorLog(datafile, successFilePath);
                    } while (new File(successFilePath).exists());
                }
            }
        }
        return successFilePath;
    }


    /**
     * 获取锁，处理错误日志
     * <p>
     * 1、尝试获取锁，如果能够获取到，说明没有其他人在操作error.log。
     * 2、将error.log中的全部内容读写到目标目录下，并删除原error.log中的内容。
     * 3、释放锁。
     * <p>
     * RandomAccessFile：
     * mode：指定打开文件的访问模式
     * rw：打开以便读取和写入，如果该文件尚不存在，则尝试创建该文件
     * <p>
     * tryLock()表示尝试获取锁，获取成功返回true，获取失败（即锁已被其他线程获取），返回false。
     * 这个方法无论如何都会立即返回。
     */
    void lockAndMove(String sourceFilePath, String targetFilePath) {
        RandomAccessFile fromFile = null;
        FileChannel fromFileChannel = null;
        FileLock fromFileLock = null;
        RandomAccessFile toFile = null;
        FileChannel toFileChannel = null;

        //判断入参不为空，是一个文件。
        if (sourceFilePath != null && !Objects.equals(sourceFilePath, "")
                && targetFilePath != null && !Objects.equals(targetFilePath, "")) {
            File sourceFile = new File(sourceFilePath);
            File targetFile = new File(targetFilePath);

            if (sourceFile.isFile() && sourceFile.exists() && sourceFile.length() != 0) {
                try {
                    while (true) {
                        fromFile = new RandomAccessFile(sourceFile, "rw");
                        fromFileChannel = fromFile.getChannel();
                        fromFileLock = fromFileChannel.tryLock();
                        if (fromFileLock == null) { //不能获取到锁
                            break;
                        } else { //能够获取到锁后的操作：
                            //1、判断目标路径（包括父目录与文件路径）是否存在，不存在，先创建。
                            File targetFolderPath = targetFile.getParentFile();
                            //若目标文件父目录不存在，先创建
                            if (!targetFolderPath.exists()) {
                                targetFolderPath.mkdirs();
                            }
                            //若目标文件不存在，先创建
                            if (!targetFile.exists()) {
                                try {
                                    targetFile.createNewFile();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                            //2、新建写入文件通道toFileChannel，通过通道复制文件fromFile。
                            toFile = new RandomAccessFile(targetFile, "rw");
                            toFileChannel = toFile.getChannel();
                            long length = fromFileChannel.size();
                            int position = 0;
                            // transferTo()：position 开始位置，count 要读取的字节数，target 目标通道。返回实际转化的字节数
                            fromFileChannel.transferTo(position, length, toFileChannel);
                            //写入新文件后，将原来的error.log清空
                            fromFileChannel.truncate(0); //截取一个文件，删除指定长度后面的部分。
                            break;
                        }
                    }
                } catch (Exception e) {
                    LOG.info("Another thread is operating this file. ");
                    e.printStackTrace();
                } finally {
                    try {
                        if (fromFileLock != null) {
                            fromFileLock.release();
                        }
                        if (fromFileChannel != null) {
                            fromFileChannel.close();
                        }
                        if (fromFile != null) {
                            fromFile.close();
                        }
                        if (toFileChannel != null) {
                            toFileChannel.close();
                        }
                        if (toFile != null) {
                            toFile.close();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }


    /**
     * 获取错误日志的随机重命名。
     * 处理错误日志时，需要把它从process目录，移动到success或merge下，并重命名
     * 重命名的规范为：原文件名+文件最后修改时间（年-月-日 时分秒）+随机数
     * 例如： error.log -> error 2018-02-01-1522148-1758.log
     *
     * @param sourceFile 错误日志的源文件绝对路径：
     *                   /data/process/p-0/error/error.log（用来获取文件最后修改时间）
     * @param targetFile 错误日志要移动到的但未重命名的目标文件绝对路径：
     *                   /success/process/201802/p-0/error/error.log（用来获取拼接绝对路径）
     * @return 重命名后的错误日志绝对路径：/success/process/p-0/error/error 2018-02-01-1522148-1758.log
     */
    private String renameErrorLog(String sourceFile, String targetFile) {
        File errorFile = new File(targetFile);
        //获取文件的父目录
        String folderPath = errorFile.getParent() + File.separator;
        //获取原文件名，去除文件后缀名
        String oldFileName = ERR_FILE_NAME.replace(SUFFIX, "");
        //获取文件最后修改时间
        String date = getFileLastModified(sourceFile, ERR_DATE_FORMAT);
        //生成随机数
        String random = Integer.toString(new Random().nextInt());

        return folderPath + oldFileName + date + random + SUFFIX;
    }


    /**
     * 获取文件最后一次修改时间。
     *
     * @param file   文件绝对路径
     * @param format 时间格式
     * @return 文件最后一次修改时间
     */
    private String getFileLastModified(String file, String format) {
        java.text.SimpleDateFormat df = new java.text.SimpleDateFormat(format);
        //获取文件最后修改时间的年-月，例如：2018-01
        return df.format(new Date(new File(file).lastModified()));
    }

}

