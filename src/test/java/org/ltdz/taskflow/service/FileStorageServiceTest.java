package org.ltdz.taskflow.service;

import org.junit.jupiter.api.Test;
import org.ltdz.taskflow.model.po.AssetPo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.Resource;
import org.springframework.mock.web.MockMultipartFile;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * {@link FileStorageService} 单元测试。
 * <p>
 * 使用 application.yaml 中真实的 {@code taskflow.storage.root}（./data/assets），
 * 测试产物直接落在配置目录下，不做额外清理。
 */
@SpringBootTest
class FileStorageServiceTest {

    /** 桌面上用于真实上传测试的 PDF */
    private static final String DESKTOP_PDF =
            "C:/Users/26412/Desktop/肖秀荣+徐涛+腿姐：27考研政治必背考点汇总.pdf";

    @Value("${taskflow.storage.root}")
    private String root;

    @Autowired
    private FileStorageService service;

    @Test
    void save_shouldWriteFileAndFillAsset() throws Exception {
        byte[] content = "hello taskflow".getBytes();
        MockMultipartFile file = new MockMultipartFile(
                "file", "测试文档.PDF", "application/pdf", content);

        AssetPo asset = service.save(file, 1001L, 2001L, 1);

        assertNotNull(asset.getId());
        assertEquals("测试文档.PDF", asset.getTitle());
        assertEquals("pdf", asset.getSuffix(), "后缀应统一转小写");
        assertEquals(1, asset.getIsInput());
        assertNotNull(asset.getUpdateTime());

        // address 应为任务目录：{root}/{taskflowId}/{taskId}
        String expectedAddress = Paths.get(root, "1001", "2001")
                .toAbsolutePath().normalize().toString();
        assertEquals(expectedAddress, asset.getAddress());
        assertTrue(asset.getAddress().replace('\\', '/').contains("taskflow-server/data/assets"),
                "文件应落在项目 data/assets 下，实际：" + asset.getAddress());

        Path saved = service.resolve(asset);
        assertTrue(Files.exists(saved), "文件应已落盘");
        assertArrayEquals(content, Files.readAllBytes(saved));
        assertEquals(asset.getId() + ".pdf", saved.getFileName().toString(),
                "磁盘文件名应为 assetId.pdf，不含原始中文名");
    }

    @Test
    void save_shouldRejectEmptyFile() {
        MockMultipartFile empty = new MockMultipartFile(
                "file", "empty.txt", "text/plain", new byte[0]);

        assertThrows(IllegalArgumentException.class,
                () -> service.save(empty, 1002L, 2002L, 1));
    }

    @Test
    void save_shouldHandleFileWithoutSuffix() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "README", "text/plain", "x".getBytes());

        AssetPo asset = service.save(file, 1003L, 2003L, 0);

        assertEquals("", asset.getSuffix());
        assertEquals(String.valueOf(asset.getId()), service.resolve(asset).getFileName().toString(),
                "无后缀时磁盘文件名不应带点");
    }

    @Test
    void load_shouldReturnReadableResource() throws Exception {
        byte[] content = "download me".getBytes();
        MockMultipartFile file = new MockMultipartFile(
                "file", "a.txt", "text/plain", content);
        AssetPo asset = service.save(file, 1004L, 2004L, 1);

        Resource resource = service.load(asset);

        assertTrue(resource.exists());
        try (InputStream in = resource.getInputStream()) {
            assertArrayEquals(content, in.readAllBytes());
        }
    }

    @Test
    void load_shouldThrowWhenFileMissing() {
        AssetPo asset = new AssetPo();
        asset.setId(999L);
        asset.setSuffix("pdf");
        asset.setAddress(Paths.get(root, "1005", "2005").toAbsolutePath().normalize().toString());

        assertThrows(IllegalStateException.class, () -> service.load(asset));
    }

    @Test
    void delete_shouldRemoveFile() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "del.txt", "text/plain", "x".getBytes());
        AssetPo asset = service.save(file, 1006L, 2006L, 1);
        Path path = service.resolve(asset);
        assertTrue(Files.exists(path));

        service.delete(asset);

        assertFalse(Files.exists(path));
    }

    @Test
    void deleteByTaskflow_shouldRemoveWholeDirectory() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "x.txt", "text/plain", "x".getBytes());
        service.save(file, 1007L, 2007L, 1);

        Path taskflowDir = Paths.get(root, "1007").toAbsolutePath().normalize();
        assertTrue(Files.exists(taskflowDir));

        service.deleteByTaskflow(1007L);

        assertFalse(Files.exists(taskflowDir), "整个任务流目录应被删除");
    }

    /**
     * 用桌面上的真实 PDF 走一遍上传，验证大文件能完整落盘。
     */
    @Test
    void save_realDesktopPdf() throws Exception {
        Path pdf = Paths.get(DESKTOP_PDF);
        assumeTrue(Files.exists(pdf), "桌面 PDF 不存在，跳过该用例");

        long sourceSize = Files.size(pdf);

        try (InputStream in = Files.newInputStream(pdf)) {
            MockMultipartFile file = new MockMultipartFile(
                    "file", pdf.getFileName().toString(), "application/pdf", in);

            AssetPo asset = service.save(file, 3001L, 4001L, 1);
            Path saved = service.resolve(asset);

            assertEquals("pdf", asset.getSuffix());
            assertEquals(pdf.getFileName().toString(), asset.getTitle());
            assertTrue(Files.exists(saved));
            assertEquals(sourceSize, Files.size(saved), "落盘大小应与源文件完全一致");
            assertEquals(asset.getId() + ".pdf", saved.getFileName().toString());

            System.out.println("[real-pdf] 源文件大小 = " + sourceSize + " 字节");
            System.out.println("[real-pdf] 落盘路径   = " + saved);
            System.out.println("[real-pdf] 落盘大小   = " + Files.size(saved) + " 字节");
        }
    }
}
