package com.example.employee.service.impl;

import com.example.employee.dto.AdjustSalaryRequest;
import com.example.employee.dto.EmployeeRequest;
import com.example.employee.dto.EmployeeResponse;
import com.example.employee.entity.Department;
import com.example.employee.entity.Employee;
import com.example.employee.repository.DepartmentRepository;
import com.example.employee.repository.EmployeeRepository;
import com.example.employee.service.EmployeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class EmployeeServiceImpl implements EmployeeService {
    @Autowired
    private EmployeeRepository employeeRepository;
    @Autowired
    private DepartmentRepository departmentRepository;
     private static final Logger logger = LoggerFactory.getLogger(EmployeeServiceImpl.class);

    private final Map<Long, LocalDateTime> lastAdjustmentMap = new HashMap<>();

    private EmployeeResponse mapToResponse(Employee employee) {
        EmployeeResponse response = new EmployeeResponse();
        response.setId(employee.getId());
        response.setName(employee.getName());
        response.setEmail(employee.getEmail());
        response.setSalary(employee.getSalary());
        response.setJoiningDate(employee.getJoiningDate());
        if (employee.getDepartment() != null) {
            response.setDepartmentId(employee.getDepartment().getId());
            response.setDepartmentName(employee.getDepartment().getName());
        }
        return response;
    }

    @Override
    public EmployeeResponse createEmployee(EmployeeRequest request) {
        Employee employee = new Employee();
        employee.setName(request.getName());
        employee.setEmail(request.getEmail());
        employee.setSalary(request.getSalary());
        employee.setJoiningDate(request.getJoiningDate());
        if (request.getDepartmentId() != null) {
            Optional<Department> dept = departmentRepository.findById(request.getDepartmentId());
            dept.ifPresent(employee::setDepartment);
        }
        Employee saved = employeeRepository.save(employee);
        return mapToResponse(saved);
    }

    @Override
    public EmployeeResponse getEmployee(Long id) {
        Optional<Employee> employee = employeeRepository.findById(id);
        return employee.map(this::mapToResponse).orElse(null);
    }

    @Override
    public EmployeeResponse updateEmployee(Long id, EmployeeRequest request) {
        Optional<Employee> optional = employeeRepository.findById(id);
        if (optional.isPresent()) {
            Employee employee = optional.get();
            employee.setName(request.getName());
            employee.setEmail(request.getEmail());
            employee.setSalary(request.getSalary());
            employee.setJoiningDate(request.getJoiningDate());
            if (request.getDepartmentId() != null) {
                Optional<Department> dept = departmentRepository.findById(request.getDepartmentId());
                dept.ifPresent(employee::setDepartment);
            }
            Employee updated = employeeRepository.save(employee);
            return mapToResponse(updated);
        }
        return null;
    }

    @Override
    public void deleteEmployee(Long id) {
        employeeRepository.deleteById(id);
    }

    @Override
    public List<EmployeeResponse> getAllEmployees() {
        return employeeRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
   public void adjustSalary(AdjustSalaryRequest request) {
        Long departmentId = request.getDepartmentId();
        int performanceScore = request.getPerformanceScore();

        // Idempotency: Prevent duplicate adjustments within 30 minutes
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime lastAdjustment = lastAdjustmentMap.get(departmentId);
        if (lastAdjustment != null && ChronoUnit.MINUTES.between(lastAdjustment, now) < 30) {
            logger.warn("Salary adjustment for department {} was already performed within the last 30 minutes.", departmentId);
            return;
        }

        Department department = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new IllegalArgumentException("Department not found"));

        List<Employee> employees = employeeRepository.findAll().stream()
                .filter(e -> e.getDepartment() != null && e.getDepartment().getId().equals(departmentId))
                .collect(Collectors.toList());

        for (Employee employee : employees) {
            double originalSalary = employee.getSalary();
            double newSalary = originalSalary;

            // 1. Performance-based adjustment
            if (performanceScore >= 90) {
                newSalary *= 1.15;
            } else if (performanceScore >= 70) {
                newSalary *= 1.10;
            } else {
                logger.warn("No salary increase for employee {} (ID: {}) due to low performance score: {}", employee.getName(), employee.getId(), performanceScore);
                continue; 
            }

            // 2. Hidden tenure bonus
            if (employee.getJoiningDate() != null && employee.getJoiningDate().isBefore(LocalDate.now().minusYears(5))) {
                newSalary *= 1.05;
            }

            
            if (newSalary > 200000) {
                newSalary = 200000;
            }

            
            if (newSalary != originalSalary) {
                employee.setSalary(newSalary);
                employeeRepository.save(employee);
            }
        }

        lastAdjustmentMap.put(departmentId, now);
    }
}