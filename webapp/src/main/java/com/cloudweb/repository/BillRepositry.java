package com.cloudweb.repository;

import com.cloudweb.entity.Bill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;

import javax.transaction.Transactional;
import java.util.List;
import java.util.UUID;

@Transactional
public interface BillRepositry extends JpaRepository<Bill,Long> {

         List<Bill> findByOwnerId(UUID uuid);
         Bill findById(UUID uuid);
         Long deleteBillById(UUID id );

}
