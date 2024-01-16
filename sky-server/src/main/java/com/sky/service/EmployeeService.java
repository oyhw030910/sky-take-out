package com.sky.service;

import com.sky.dto.EmployeeDTO;
import com.sky.dto.EmployeeLoginDTO;
import com.sky.dto.EmployeePageQueryDTO;
import com.sky.dto.PasswordEditDTO;
import com.sky.entity.Employee;
import com.sky.result.PageResult;
import com.sky.result.Result;

public interface EmployeeService {



    /**
     * 员工登录
     * @param employeeLoginDTO
     * @return
     */
    Employee login(EmployeeLoginDTO employeeLoginDTO);
    void save(EmployeeDTO employeeDTO);

    PageResult pageQuery(EmployeePageQueryDTO employeePageQueryDTO);

    void ChangeStatus(Employee employee);

    Employee getById(long id);

    void updateEmployee(EmployeeDTO employeeDTO);

    void changePassword(PasswordEditDTO passwordEditDTO);
}
