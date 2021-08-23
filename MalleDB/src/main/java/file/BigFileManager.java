package file;

import db.MalleDB;
import org.apache.commons.math3.random.RandomGenerator;
import org.springframework.web.multipart.MultipartFile;
import util.Item;
import util.MetaFile;
import util.Options;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.charset.StandardCharsets;

public class BigFileManager {

    MalleDB malleDB;

    public BigFileManager(MalleDB malleDB) {
        this.malleDB = malleDB;
    }

    public String bigFileInsert(MultipartFile multipartFile) throws IOException {
        String fileName = multipartFile.getOriginalFilename();
        long size = multipartFile.getSize();
        InputStream fileStream = multipartFile.getInputStream();

        System.out.println("BIG FILE INSERT");

//        File file = new File(fileName);
//        file.createNewFile();
//        FileOutputStream fos = new FileOutputStream(file);
//        fos.write(multipartFile.getBytes()); // multipartFile -> File
//        fos.close();
//        multipartFile.transferTo(file);
//        FileInputStream fis = new FileInputStream(file);
//        BufferedInputStream bis = new BufferedInputStream(fileStream, Options.BUFFER_SIZE);

        //RandomAccessFile raf = new RandomAccessFile(filepath, "r");
        int sourceSize = Long.valueOf(size).intValue();
        int chunkCount = sourceSize / Options.BUFFER_SIZE + 1;
//        int remainingBytes = sourceSize % Options.BUFFER_SIZE;

        // Insert MetaFile for BigFile
        MetaFile metaFile = new MetaFile(sourceSize, fileName, 2, chunkCount);
        malleDB.insert(metaFile.getid(), metaFile.toString());

        byte[] buf = new byte[Options.BUFFER_SIZE];
        int chunkNum = 1;
        while(fileStream.read(buf) != -1)
            malleDB.insert(metaFile.getid() + chunkNum++, FileManager.encoder(buf));

        fileStream.close();
//        fis.close();

//        System.out.println("filename DELETE: " + multi.getName());
//        file.delete();  // Delete the file we converted from multipartFile

        return metaFile.getid();
    }

    public String bigFileRead(MetaFile metaFile) throws IOException {
        String metaID = metaFile.getid();
        int chunkCount = metaFile.getN();
        String fileName = metaFile.getname();

        FileOutputStream fos = new FileOutputStream("./demo/" + fileName);
        BufferedOutputStream bos = new BufferedOutputStream(fos);

        for (int chunkNum = 1; chunkNum <= chunkCount; chunkNum++) {
            String chunk = malleDB.read(metaID + chunkNum).getValue();
            bos.write(FileManager.decoder(chunk));
        }

        bos.close();
        fos.close();

        return fileName;
    }

    public String bigFileUpdate(MetaFile metaFile, MultipartFile multipartFile) throws IOException {
        bigFileDelete(metaFile);
        return bigFileInsert(multipartFile);    // return new metaId
    }

    public void bigFileDelete(MetaFile metaFile){
        String metaID = metaFile.getid();
        int chunkCount = metaFile.getN();
        String fileName = metaFile.getname();

        for (int chunkNum = 1; chunkNum <= chunkCount; chunkNum++)
            malleDB.delete(metaID + chunkNum);

        metaFile.setDeleted(true);
        malleDB.update(metaID, metaFile.toString());
    }

    static String getFileName(String filePath) {
        int lastSlashIdx = filePath.lastIndexOf('\\');
        if (lastSlashIdx == -1) {
            return filePath;
        } else {
            return filePath.substring(lastSlashIdx + 1);
        }
    }

}