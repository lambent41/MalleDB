package file;

import java.io.IOException;

import db.MalleDB;
import org.springframework.web.multipart.MultipartFile;
import util.*;
import java.io.*;
import java.util.Base64;

public class FileManager {
    private final MalleDB malleDB;
    private final SmallFileManager smallFileManager;
    private final MiddleFileManager middleFileManager;
    private final BigFileManager bigFileManager;
    private String prefix = "123456";

    public int isBig(MultipartFile multipartFile) {
        long size = multipartFile.getSize();
        System.out.println(size);
        if (size > Options.BUFFER_SIZE + 1024) return 2;
        else if (size < Options.BUFFER_SIZE) return 1;
        else {
            return 0;
        }
    }

    public FileManager(MalleDB malleDB) {
        this.malleDB = malleDB;
        smallFileManager = new SmallFileManager(malleDB);
        middleFileManager = new MiddleFileManager(malleDB);
        bigFileManager = new BigFileManager(malleDB);
    }

    public Status insertMetaFile(MetaFile newmeta) {
        String key;
        String metaInfo;
        metaInfo = newmeta.toString();
        key = prefix + newmeta.getKey();//change random generate key
        return malleDB.insert(key, metaInfo);
    }

    public Status updateMetaFile(String key, MetaFile newmeta) {
        //뉴 메타 파일의 정보에서 기존 메타파일과 동일점 찾아서 업데이트
        deleteMetaFile(key);
        String metaInfo = newmeta.toString();
        return malleDB.insert(key, metaInfo);
    }

    public void deleteMetaFile(String key) {
        malleDB.delete(key);
    }

    public MetaFile readMetaFile(String key) {
        String value = malleDB.read(key).getValue();
        MetaFile metaFile = new MetaFile();
        metaFile.Stringto(value);
        return metaFile;
    }

    public String insertFile(MultipartFile multipartFile) throws IOException {
        String fileName = multipartFile.getOriginalFilename();
        System.out.println("Inserting File : " + fileName);
        //일부 파일은 파일 path를 인자로 받는중

        String metaId = "";

        if (isBig(multipartFile) == 2) {
            metaId = bigFileManager.bigFileInsert(multipartFile);
        } else if (isBig(multipartFile) == 1) {
            //middleFileManager.middleFileInsert(multipartFile);
        } else {
            //smallFileManager.smallFileInsertEncoder(multipartFile);
        }

        return metaId;
    }

//    public Status insertFile2(String filepath) throws IOException {
//
//        String filename = bigFileManager.getFileName(filepath);
//
//        System.out.println("Inserting File : " + filename);
//        //일부 파일은 파일 path를 인자로 받는중
//        File file = new File(filepath);
//        int listSize = 10;
//        int listCsr = 0;
//
//        MetaFile tempMeta = smallFileManager.link;
//        if ((((listCsr + 1) % listSize == 0) && (listCsr != 0)) || listCsr + 1 == listSize) {//this is isListFull() or all MetaFile read
//            System.out.println("Insert linkedlist");
//            for (; listCsr < 0; listCsr--) {
//
//                malleDB.insert(tempMeta.getid(), tempMeta.toString());
//                tempMeta = tempMeta.getLink();
//            }
//        }
//
//        //MetaFile의 info 정의
//        MetaFile meta = new MetaFile();
//        int sourceSize = Long.valueOf(file.length()).intValue();
//
//        if (isBig(filename) == 2) {
//            bigFileManager.bigFileInsert(filename);
//        } else if (isBig(filename) == 1) {
//            middleFileManager.middleFileInsert(filename);
//        } else {
//            if (malleDB.smallFilesbuffer == null) {
//                malleDB.smallFilesbuffer = new byte[sourceSize];
//            }
//            smallFileManager.smallOneFileInsert(filepath, malleDB.smallFilesbuffer, sourceSize);//
//        }//insert 방식을 어떻게 filepath에서 filename으로 바꿀것인가
//
//
//        // 모든 파일이 경로가 다르면, 현재의 스몰파일의 경우 버퍼의 지속성이 어려워진다.
//        // 그렇기에 파일네임으로만 스몰파일인서트를 실행하고 싶으면 외부에서 버퍼를 받아와야한다.
//        //버퍼의 지속성 문제
//        //버퍼는 내부에서 정의 된후 유지되어야한다. 가비지 처리 되나 안되나?
//        //리턴주소를 버퍼로 하면 어떨까
//        //malleDB내에 버퍼를 정의하면 될수도있다.
//
//
//        //혹은 스몰파일 인서트시 버퍼정의시 이전 파일의 메타파일에 저장된 버퍼주소를 불러오는 형식은 어떨까
//        //이것은 물론 가비지 컬렉션을 피해야함
//
//        meta.setid("Meta_temp" + file.getName());
//        meta.setname(file.getName());
//        meta.setsize((int) file.length());
//        if (listCsr != 0) {
//            meta.setLink(smallFileManager.link);
//        }
//        smallFileManager.link = meta;
//        listCsr++;
//
//
//
//
//       /* int listize = Long.valueOf(file.length()).intValue()/Options.BUFFER_SIZE + 1;
//        int listCsr=0;
//        LinkedList<MetaFile> listOfMetaFiles= new LinkedList<MetaFile>();
//        while(true){
//
//            if( (( (listCsr+1)%listSize == 0) && ( listCsr!=0 )) || listCsr+1 == listSize ) {//this is isListFull() or all MetaFile read
//                MetaFile tempMeta;
//                int cycle = (listCsr)%listSize;//if size = 10 , cycle = 9
//                for(int i = 0; i< cycle;  i++ ){
//                    tempMeta = listOfMetaFiles.get(i);
//                    malleDB.insert(tempMeta.getid(),tempMeta.toString());
//                }
//                if(listCsr+1 == listSize)break;
//            }
//
//            //MetaFile의 info 정의
//            MetaFile meta = new MetaFile();
//            meta.setid("Meta_" + file.getName());
//            meta.setname(file.getName());
//            meta.setsize((int) file.length());
//            listOfMetaFiles.add((listCsr)%listSize,meta);
//            listCsr++;
//        }
//         */
//        //주석은 파라미터 파일패스일때 다수 파일에 대한 메타파일 제작 구문이다.//오류코드 참고만하다 삭제
//
//        //일단은 프로토로. 개수로 사이즈가 정해짐. 추후 크기를 이용하여 리스트의 사이즈를 변경할수도있음
//
//
//        return Status.OK;
//    }

    public Status readFile(String metaID) throws IOException {
        Status metaStatus = malleDB.read(metaID);

        if(metaStatus.equals(Status.NOT_FOUND)){
            // 해당 파일 없음
            return Status.NOT_FOUND;
        }

        String metaFileString = metaStatus.getValue();
        MetaFile metaFile = new MetaFile();
        metaFile.Stringto(metaFileString);

        if(metaFile.isDeleted()){
            return Status.NOT_FOUND;
        }

        String fileName = "";

        if (metaFile.isBig() == 2) {
            fileName = bigFileManager.bigFileRead(metaFile);
        } else if (metaFile.isBig() == 1) {
            //  middleFileManager.middleFileRead(metaFile);

        } else {
            //smallFileManager.smallFileDataRead(metaFile.getid());
            smallFileManager.smallOneFileRead(metaID);
        }

        Status status = new Status(fileName, "READ SUCCESS");
        return status;
    }

    public Status updateFile(String metaID, MultipartFile multipartFile) throws IOException {
        Status metaStatus = malleDB.read(metaID);
        if(metaStatus.equals(Status.NOT_FOUND)){
            // 해당 파일 없음
            return Status.NOT_FOUND;
        }

        String metaFileString = malleDB.read(metaID).getValue();
        MetaFile metaFile = new MetaFile();
        metaFile.Stringto(metaFileString);

        String newMetaId = "";

        if (metaFile.isBig() == 2) {
            newMetaId = bigFileManager.bigFileUpdate(metaFile, multipartFile);
        } else if (metaFile.isBig() == 1) {
            //middleFileManager.middleFileUpdate(metaFile);
        } else {
            // Small File Update
        }

        return new Status(newMetaId, "UPDATE SUCCESS");
    }

    public Status deleteFile(String metaID) {
        Status metaStatus = malleDB.read(metaID);
        if(metaStatus.equals(Status.NOT_FOUND)){
            // 해당 파일 없음
            return Status.NOT_FOUND;
        }

        String metaFileString = metaStatus.getValue();
        MetaFile metaFile = new MetaFile();
        metaFile.Stringto(metaFileString);

        if(metaFile.isDeleted()){
            return Status.NOT_FOUND;
        }

        if (metaFile.isBig() == 2) {
            bigFileManager.bigFileDelete(metaFile);
        } else if (metaFile.isBig() == 1) {
            middleFileManager.middleFileDelete(metaFile);
        } else {
            // Small File Update

        }

        Status returnStatus = new Status(metaFile.getname(), "DELETE SUCCESS");
        return returnStatus;
    }


    public static String encoder(byte[] imageData) {
        System.out.println("ENCODING");
        return Base64.getEncoder().encodeToString(imageData);
    }

    public static byte[] decoder(String base64Image) {
        return Base64.getDecoder().decode(base64Image);
    }

}
