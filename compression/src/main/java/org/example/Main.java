package org.example;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.encryption.AccessPermission;
import org.apache.pdfbox.pdmodel.encryption.StandardProtectionPolicy;

import java.io.File;
import java.net.URISyntaxException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main {
    private static final Logger logger = Logger.getLogger(Main.class.getName());
    public static void main(String[] args) {
        try {
            if (args.length == 0) {
                logger.severe("未提供密碼！請使用以下格式執行程式：");
                logger.info("java -jar encryptor.jar <password>");
                return;
            }

            // 取得密碼
            String password = args[0];
            logger.info("使用者輸入的密碼: " + password);

            // 取得與 JAR 檔相同目錄的路徑 測試
//            String currentPath = new File(".").getAbsolutePath();
//            File currentFolder = new File(currentPath);
//            logger.info("當前目錄: " + currentPath);
            // 取得 JAR 檔所在的目錄 正式
        String jarDir = getExecutionDirectory();
        if (jarDir == null) {
            logger.severe("無法取得 JAR 檔的目錄位置！");
            return;
        }
        logger.info("執行目錄: " + jarDir);
        // 指定目錄
        File currentFolder = new File(jarDir);
            if (currentFolder.exists()) {
                // 取得目錄中所有的 PDF 檔案
                File[] pdfFiles = currentFolder.listFiles((dir, name) -> name.toLowerCase().endsWith(".pdf") && !name.toLowerCase().endsWith("_encrypted.pdf"));

                if (pdfFiles != null && pdfFiles.length > 0) {
                    // 遍歷並加密每個 PDF 檔案
                    for (File pdfFile : pdfFiles) {
                        encryptPDF(pdfFile, password);
                    }
                } else {
                    logger.warning("當前目錄沒有找到任何 PDF 檔案。");
                }
            } else {
                logger.info("找不到當前目錄");
            }
        }catch (Exception e){
            logger.log(Level.SEVERE, "發生錯誤：", e);
        }

    }

    private static void encryptPDF(File pdfFile, String password) {
        try (PDDocument document = PDDocument.load(pdfFile)) {
            logger.info("正在加密檔案: " + pdfFile.getName());
            // 設定存取權限 (允許列印、複製等)
            AccessPermission accessPermission = new AccessPermission();

            // 設定加密保護
            StandardProtectionPolicy spp = new StandardProtectionPolicy(password, password, accessPermission);
            spp.setEncryptionKeyLength(128); // 使用 128 位元加密
            spp.setPermissions(accessPermission);

            // 加密文件
            document.protect(spp);

            // 新的檔案名稱，加上 "_encrypted" 後綴
            String newFileName = pdfFile.getAbsolutePath().replace(".pdf", "_encrypted.pdf");
            File encryptedFile = new File(newFileName);

            // 儲存加密後的檔案，覆寫原檔案
            document.save(encryptedFile);

            logger.info("加密成功，新的檔案位置: " + encryptedFile.getAbsolutePath());
        } catch (Exception e) {
            logger.log(Level.SEVERE, "加密失敗: " + pdfFile.getAbsolutePath(), e);
            e.printStackTrace();
        }
    }

    // 取得 JAR 檔執行時的目錄
    private static String getExecutionDirectory() {
        try {
            // 取得當前執行的 JAR 檔或應用檔案的路徑
            File jarFile = new File(Main.class.getProtectionDomain().getCodeSource().getLocation().toURI());

            // 如果是在 macOS 且是 .app 結構
            if (jarFile.getAbsolutePath().contains(".app")) {
                    // 定位到 .app 的上一層目錄
                File appDir = jarFile.getParentFile().getParentFile(); // 這樣會返回到 .app 包裡的 Contents
                File appRootParent = appDir.getParentFile().getParentFile(); // 返回 .app 根目錄所在的上一層（即我們的 output 目錄）
                return appRootParent.getAbsolutePath();
            }

            // 否則，返回 JAR 或執行檔所在的目錄
            return jarFile.getParent();
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return null;
        }
    }
}