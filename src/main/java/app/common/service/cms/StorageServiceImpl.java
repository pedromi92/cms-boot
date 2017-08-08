package app.common.service.cms;

import app.common.service.cms.api.StorageService;
import app.persistence.entity.cms.Page;
import app.persistence.entity.cms.PageImage;
import app.persistence.repository.cms.PageImageRepository;
import groovy.lang.Singleton;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @author Samuel Butta
 */
@Service
@Singleton
public class StorageServiceImpl implements StorageService {


    @Value("${upload.work-dir}")
    private String uploadWorkdir;

    private static final String WORK_IMAGE = "image";

    private Path uploadPath;

    private PageImageRepository pageImageRepository;

    @Autowired
    public StorageServiceImpl(PageImageRepository pageImageRepository) {
        this.pageImageRepository = pageImageRepository;
    }

    @PostConstruct
    private void init() {
        this.uploadPath = Paths.get(uploadWorkdir).resolve(WORK_IMAGE);
    }

    @Override
    public void store(MultipartFile file, String identity, Page page) {
        String fileName = StringUtils.cleanPath(file.getOriginalFilename());

        // PageImage pageImage = PageImage.builder().name(fileName).page(page).build(); FIXME
        PageImage pageImage = new PageImage();
        pageImage.setFileName(fileName);
        pageImage.setIdentity(identity);
        pageImage.setPage(page);

        pageImageRepository.save(pageImage);

        if(file.isEmpty() || fileName.contains("..")) {
            // TODO invalid file exception
        }

        try {
            Files.copy(file.getInputStream(), uploadPath.resolve(fileName));
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public Resource loadAsResource(String fileName) {
        try {
            Path file = uploadPath.resolve(fileName);
            Resource resource = new UrlResource(file.toUri());

            return resource;
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return null;
    }

}