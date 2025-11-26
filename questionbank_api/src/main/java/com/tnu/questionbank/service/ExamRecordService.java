package com.tnu.questionbank.service;

import com.tnu.questionbank.dto.ExamRecordSummaryDTO;
import com.tnu.questionbank.mapper.ExamRecordMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ExamRecordService {

    @Autowired
    private ExamRecordMapper examRecordMapper;

    public List<ExamRecordSummaryDTO> listByStudent(Integer studentId) {
        return examRecordMapper.findSummaryByStudentId(studentId);
    }
}
