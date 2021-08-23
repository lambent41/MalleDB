package com.api.demo.component;

import db.MalleDB;
import file.FileManager;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import util.Options;
import util.Status;

import java.io.IOException;
import java.io.InputStream;

@Component(value = "MalleDB")
public class ApplicationMalleDB {

    MalleDB malleDB;
    FileManager fileManager;

    public ApplicationMalleDB() {
        this.malleDB = new MalleDB();
        malleDB.init(new Options(Options.DB_TYPE.LEVELDB));
        fileManager = new FileManager(malleDB);
    }

    public String upload(MultipartFile file) throws IOException {
        return fileManager.insertFile(file);
    }

    public Status download(String metaId) throws IOException{
        return fileManager.readFile(metaId);
    }

    public Status delete(String metaId){
        return fileManager.deleteFile(metaId);
    }

    public Status update(String metaId, MultipartFile multipartFile) throws IOException {
        return fileManager.updateFile(metaId, multipartFile);
    }

    public Status insertKV(String key, String value){
        return malleDB.insert(key, value);
    }

        public Status readKV(String key){
            return malleDB.read(key);
        }

        public Status deleteKV(String key){
            return malleDB.delete(key);
    }

    public Status updateKV(String key, String value){
        return malleDB.update(key, value);
    }
}
