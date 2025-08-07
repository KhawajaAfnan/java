package com.example.employee.service.impl;

import com.example.employee.dto.DepartmentRequest;
import com.example.employee.dto.DepartmentResponse;
import com.example.employee.entity.Department;
import com.example.employee.repository.DepartmentRepository;
import com.example.employee.service.DepartmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class DepartmentServiceImpl implements DepartmentService {
    @Autowired
    private DepartmentRepository departmentRepository;

    private DepartmentResponse mapToResponse(Department department) {
        DepartmentResponse response = new DepartmentResponse();
        response.setId(department.getId());
        response.setName(department.getName());
        response.setCode(department.getCode());
        return response;
    }

    @Override
    public DepartmentResponse createDepartment(DepartmentRequest request) {
        Department department = new Department();
        department.setName(request.getName());
        department.setCode(request.getCode());
        Department saved = departmentRepository.save(department);
        return mapToResponse(saved);
    }

    @Override
    public DepartmentResponse getDepartment(Long id) {
        Optional<Department> department = departmentRepository.findById(id);
        return department.map(this::mapToResponse).orElse(null);
    }

    @Override
    public DepartmentResponse updateDepartment(Long id, DepartmentRequest request) {
        Optional<Department> optional = departmentRepository.findById(id);
        if (optional.isPresent()) {
            Department department = optional.get();
            department.setName(request.getName());
            department.setCode(request.getCode());
            Department updated = departmentRepository.save(department);
            return mapToResponse(updated);
        }
        return null;
    }

    @Override
    public void deleteDepartment(Long id) {
        departmentRepository.deleteById(id);
    }

    @Override
    public List<DepartmentResponse> getAllDepartments() {
        return departmentRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
}