package com.deluxeterno.service;

import com.deluxeterno.domain.Inquiry;
import com.deluxeterno.domain.InquiryStatus;
import com.deluxeterno.dto.InquiryForm;
import com.deluxeterno.repository.InquiryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional(readOnly = true)
public class InquiryService {

    private final InquiryRepository inquiryRepository;

    public InquiryService(InquiryRepository inquiryRepository) {
        this.inquiryRepository = inquiryRepository;
    }

    @Transactional
    public Inquiry create(InquiryForm form) {
        return inquiryRepository.save(new Inquiry(
                form.getName(),
                form.getEmail(),
                form.getPhone(),
                form.getMessage()));
    }

    public List<Inquiry> findAll() {
        return inquiryRepository.findAllByOrderByCreatedAtDesc();
    }

    public Page<Inquiry> findPage(String search, InquiryStatus status, int page) {
        return inquiryRepository.findAdminInquiries(
                search,
                status,
                PageRequest.of(Math.max(0, page), 20, Sort.by("createdAt").descending()));
    }

    public long countNew() {
        return inquiryRepository.countByStatus(InquiryStatus.NEW);
    }

    public long countAll() {
        return inquiryRepository.count();
    }

    @Transactional
    public void updateStatus(Long id, InquiryStatus status) {
        Inquiry inquiry = inquiryRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Consulta no encontrada"));
        inquiry.setStatus(status);
    }
}
