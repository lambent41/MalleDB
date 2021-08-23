package com.api.demo.controller;

import com.api.demo.component.ApplicationMalleDB;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import util.Status;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;

@Controller
public class DemoController {

    private final ApplicationMalleDB applicationMalleDB;

    @Autowired
    public DemoController(ApplicationMalleDB applicationMalleDB) {
        this.applicationMalleDB = applicationMalleDB;
    }

    @GetMapping("/upload")
    public String upload(Model model) {
        System.out.println("UPLOAD");
        return "upload";
    }

    @PostMapping("/upload/file")
    public String uploadFile(@RequestParam("file") MultipartFile file,
                             HttpServletRequest request,
                             Model model) throws IOException {
        System.out.println("UPLOAD FILE");

        String metaId = applicationMalleDB.upload(file);
        model.addAttribute("metaId", metaId);

//        ServletFileUpload upload = new ServletFileUpload();
//
//        FileItemIterator iterator = upload.getItemIterator(request);
//        while (iterator.hasNext()) {
//            FileItemStream item = iterator.next();
//
//            if (!item.isFormField()) {
//                InputStream inputStream = item.openStream();
//            }
//        }
        return "uploadResult";
    }

    @GetMapping("/update")
    public String update(Model model) {
        System.out.println("UPDATE");
        return "update";
    }

    @PostMapping("/update/file")
    @ResponseBody
    public String updateFile(@RequestParam("metaId") String metaId,
                             @RequestParam("file") MultipartFile file,
                             Model model) throws IOException {
        System.out.println("UPDATE FILE");
        Status updateStatus = applicationMalleDB.update(metaId, file);

        if(updateStatus.equals(Status.NOT_FOUND)){
            System.out.println("FILE NOT FOUND");
            return "NOT FOUND";
        }

        return "New Meta Id is " + updateStatus.getName() + ", UPDATE SUCCESS";
    }

    @ResponseBody
    @GetMapping("/download/{metaId}")
    public String download(@PathVariable("metaId") String metaId,
                           HttpServletResponse response) throws IOException {
        System.out.println("DOWNLOAD");

        Status readStatus = applicationMalleDB.download(metaId);
        if(readStatus.equals(Status.NOT_FOUND)){
            System.out.println("FILE NOT FOUND");

            return "NOT FOUND";
        }

        String fileName = readStatus.getName();

        response.setContentType("application/octer-stream");
        response.setHeader("Content-Transfer-Encoding", "binary;");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");

        try{
            OutputStream os = response.getOutputStream();
            FileInputStream fis = new FileInputStream("./demo/" + fileName);

            int count = 0;
            byte[] bytes = new byte[512];

            while((count = fis.read(bytes)) != -1){
                os.write(bytes, 0, count);
            }

            fis.close();
            os.close();

            File file = new File("./demo/" + fileName);
            System.out.println("filename DELETE: " + file.getName());

            file.delete();  // Delete the file we converted from multipartFile
        } catch (FileNotFoundException exception){
            System.out.println("FileNotFoundException");
        }

        return fileName;
    }

    @GetMapping("/delete/{metaId}")
    @ResponseBody
    public String delete(@PathVariable("metaId") String metaId){
        System.out.println("DELETE");

        Status deleteStatus = applicationMalleDB.delete(metaId);

        if(deleteStatus.equals(Status.NOT_FOUND)){
            return "NOT FOUND";
        }

        String deletedFile = deleteStatus.getName();
        return deletedFile +  " " + deleteStatus.getDescription();
    }

    @GetMapping("/kv/insert")
    public String insertKV(){
        System.out.println("KV INSERT");
        return "insertKV";
    }

    @PostMapping("/kv/insert/result")
    @ResponseBody
    public String insertKVResult(@RequestParam("key") String key,
                                 @RequestParam("value") String value){
        applicationMalleDB.insertKV(key, value);
        return "Key: " + key + " Value: " + value + " INSERT SUCCESS";
    }

    @GetMapping("/kv/read/{key}")
    @ResponseBody
    public String readKV(@PathVariable("key") String key){
        Status readStatus = applicationMalleDB.readKV(key);
        return readStatus.getValue();
    }

    @GetMapping("/kv/delete/{key}")
    @ResponseBody
    public String deleteKV(@PathVariable("key") String key){
        Status deleteStatus = applicationMalleDB.deleteKV(key);
        return deleteStatus.getDescription();
    }

    @GetMapping("/kv/update")
    public String updateKV(){
        System.out.println("KV UPDATE");
        return "updateKV";
    }

    @PostMapping("/kv/update/result")
    @ResponseBody
    public String updateKVResult(@RequestParam("key") String key,
                                 @RequestParam("newValue") String newValue){
        Status updateStatus = applicationMalleDB.updateKV(key, newValue);
        return updateStatus.getDescription();
    }

    @GetMapping("/upload/multiple")
    public String uploadMultiple(){
        System.out.println("UPLOAD MULTIPLE");
        return "uploadMultiple";
    }
}
