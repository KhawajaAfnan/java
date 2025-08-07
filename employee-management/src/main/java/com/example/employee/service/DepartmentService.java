package com.example.employee.service;

import com.example.employee.dto.DepartmentRequest;
import com.example.employee.dto.DepartmentResponse;
import java.util.List;

public interface DepartmentService {
    DepartmentResponse createDepartment(DepartmentRequest request);
    DepartmentResponse getDepartment(Long id);
    DepartmentResponse updateDepartment(Long id, DepartmentRequest request);
    void deleteDepartment(Long id);
    List<DepartmentResponse> getAllDepartments();
}