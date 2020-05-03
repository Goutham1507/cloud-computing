package com.cloudweb.repository;

import com.cloudweb.entity.Bill;
import com.cloudweb.entity.File;
import org.springframework.data.jpa.repository.JpaRepository;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Transactional
public interface FileRepositry extends JpaRepository<File,Long> {
    File findById(UUID uuid);
    Long deleteById(UUID uuid);
    ArrayList<File> findAllByFileName(String fileName);
    File findByBillId(UUID uuid);
    Long deleteByBillId(UUID id);

}
