package ch.controller;

import ch.bean.Employee;
import ch.bean.Msg;
import ch.service.EmployeeService;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 处理员工CRUD请求
 */
//  restful URI:
//  /emps/{id}   GET     查询员工
//  /emps        POST    保存员工
//  /emps/{id}   PUT     修改员工
//  /emps/{id}   DELETE  删除员工

@Controller
public class EmployeeController {

    @Autowired
    EmployeeService employeeService;

    /**
     * 查询员工数据（分页查询）
     *
     * @return
     */
    //@RequestMapping("/emps")
    public String getEmps(@RequestParam(value = "pn", defaultValue = "1") Integer pn,
                          Model model) {
        // 这不是一个分页查询；
        // 引入PageHelper分页插件
        // 在查询之前只需要调用，传入页码，以及每页的大小
        PageHelper.startPage(pn, 6);
        // startPage后面紧跟的这个查询就是一个分页查询
        List<Employee> emps = employeeService.getAll();
        // 使用pageInfo包装查询后的结果，只需要将pageInfo交给页面就行了。
        // 封装了详细的分页信息,包括有我们查询出来的数据，传入连续显示的页数
        PageInfo page = new PageInfo(emps, 5);
        model.addAttribute("pageInfo", page);

        return "list";
    }

    //springmvc默认使用jackson来实现对象与json之间的相互转换
    //在springmvc中，如果使用@ResponseBody 返回json格式对象，需要导入jackson的jar包
    @RequestMapping("/emps")
    @ResponseBody
    public Msg getEmpsWithJson(@RequestParam(value = "pn", defaultValue = "1") Integer pn,
                               Model model){
        // 引入PageHelper分页插件
        // 在查询之前只需要调用，传入页码，以及每页的大小
        PageHelper.startPage(pn, 6);
        // startPage后面紧跟的这个查询就是一个分页查询
        List<Employee> emps = employeeService.getAll();
        // 使用pageInfo包装查询后的结果，只需要将pageInfo交给页面就行了。
        // 封装了详细的分页信息,包括有我们查询出来的数据，传入连续显示的页数
        PageInfo page = new PageInfo(emps, 5);
        return Msg.success().add("pageInfo",page);
    }

    /**
     * 员工保存
     * 导入Hibernate-Validator(支持JSR303校验)
     * @return
     */
    //@RequestMapping(value="/emps",method= RequestMethod.POST)
    //@ResponseBody
    //public Msg saveEmp(Employee employee){
    //    employeeService.saveEmp(employee);
    //    return Msg.success();
    //}
    @RequestMapping(value="/emps", method=RequestMethod.POST)
    @ResponseBody
    public Msg saveEmp(@Valid Employee employee, BindingResult result){
    	if(result.hasErrors()){
    		//Employee中定义了校验字段，BindingResult封装校验结果，
            // 校验失败，在模态框中显示校验失败的错误信息
    		Map<String, Object> map = new HashMap<>();
    		List<FieldError> errors = result.getFieldErrors();
    		for (FieldError fieldError : errors) {
    			System.out.println("错误的字段名："+fieldError.getField());
    			System.out.println("错误信息："+fieldError.getDefaultMessage());
    			map.put(fieldError.getField(), fieldError.getDefaultMessage());
    		}
    		return Msg.fail().add("errorFields", map);
    	}else{
    		employeeService.saveEmp(employee);
    		return Msg.success();
    	}

    }


    /**
     * 检查用户名是否可用
     * @param empName
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/checkuser",method = RequestMethod.POST)
    public Msg checkuser(@RequestParam("empName")String empName){
        //先判断用户名是否是合法的表达式(java正则表达式)
        String regx = "(^[a-zA-Z0-9_-]{6,16}$)|(^[\u2E80-\u9FFF]{2,5})";
        if(!empName.matches(regx)){
            return Msg.fail().add("va_msg", "用户名必须是6-16位数字和字母的组合或者2-5位中文");
        }
        //数据库用户名重复校验
        boolean b = employeeService.checkUser(empName);
        if(b){
            return Msg.success();
        }else{
            return Msg.fail().add("va_msg", "已有相同用户名");
        }
    }



}
