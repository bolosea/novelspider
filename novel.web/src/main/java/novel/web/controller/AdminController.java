package novel.web.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import novel.web.constants.Constants;
import novel.web.entitys.JSONResponse;
import novel.web.entitys.Page;
import novel.web.entitys.User;
import novel.web.service.UserService;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

@Controller
@RequestMapping("/admin")
public class AdminController {
    @Autowired
    UserService userService;
    @RequestMapping(value = "/index")
    public ModelAndView admin(HttpServletRequest request){

        ModelAndView view = new ModelAndView();
        view.setViewName("admin/admin");
        User user = (User)request.getSession().getAttribute(Constants);
        if(user!=null){
            view.addObject("user",user);
        }
        return view;

    }

    @RequestMapping(value = "/toLogin")
    public ModelAndView toLogin(){
        ModelAndView view = new ModelAndView();
        view.setViewName("admin/login");
        return view;

    }

    @RequestMapping(value = "/novelManager")
    public ModelAndView novelManager(){
        ModelAndView view = new ModelAndView();
        view.setViewName("admin/novel-manager");
        return view;

    }

    @RequestMapping(value = "/userManager")
    public ModelAndView userManager(){
        ModelAndView view = new ModelAndView();
        view.setViewName("admin/user-manager");
        return view;

    }

    @RequestMapping(value = "/home")
    public ModelAndView home(){
        ModelAndView view = new ModelAndView();
        view.setViewName("admin/home");
        return view;

    }

    /**
     * 登录
     * @param request
     * @param param
     * @return
     */
    @RequestMapping(value = "/login",method = RequestMethod.POST)
    @ResponseBody
    public JSONResponse login(HttpServletRequest request, HttpServletResponse response,@RequestBody String param){
        Map obj = (Map)JSON.parse(param);
        User admin = new User();

        String md5Name = DigestUtils.md5Hex((String) obj.get("username"));
        String md5Pwd = DigestUtils.md5Hex((String) obj.get("password")+(String) obj.get("username"));
        admin.setName(md5Name);
        admin.setPassword(md5Pwd);
        int check = userService.check(admin);
        int status = check;
        if(status==1){
            request.getSession().setAttribute(Constants.Sys_USER,admin);
            String isRemember = obj.get("rememberMe")==null?"false":(String) obj.get("rememberMe");
            if (isRemember.equalsIgnoreCase("true")){
                Cookie cname = new Cookie("username", md5Name);
                Cookie cpwd = new Cookie("password",md5Pwd);
                cname.setPath("/");
                cpwd.setPath("/");
                cname.setMaxAge(3600);
                cpwd.setMaxAge(3600);
                response.addCookie(cname);
                response.addCookie(cpwd);
            }
            return JSONResponse.success(admin,0);
        }
        return JSONResponse.error("登录失败");
    }

    /**
     * 退出
     * @param request
     * @return
     */
    @RequestMapping(value = "/logout",method = RequestMethod.GET)
    @ResponseBody
    public ModelAndView logout(HttpServletRequest request){
        ModelAndView view = new ModelAndView();
        view.setViewName("/admin/admin");
        Object user = request.getSession().getAttribute(Constants.CURRENT_USER);
        if (user!=null){
            request.getSession().removeAttribute(Constants.CURRENT_USER);
            return view;
        }
        return null;
    }

    /**
     *
     * @param page 页码
     * @param limit 每页显示条数
     * @return
     */
    @RequestMapping(value = "/getAllUser", method = RequestMethod.GET)
    @ResponseBody
    public JSONResponse getAllUserByPage(int page, int limit) {
        Page<User> pages = userService.getAllUser(page,limit);
        return JSONResponse.success(pages.getPages(),pages.getTotalCount());
    }

    /**
     * 删除用户
     * @param id
     * @return
     */
    @RequestMapping(value = "/deleteUserById",method = RequestMethod.POST)
    @ResponseBody
    public JSONResponse deleteUserById(@RequestParam("id")long id){
        User user = userService.getUserById(id);
        if (user!=null){
            userService.delete(user);
            return JSONResponse.success(user);
        }
        return JSONResponse.error("删除失败");

    }

    /**
     * 更新用户
     * @param param
     * @return
     */
    @RequestMapping(value = "/updateUser",method = RequestMethod.POST)
    @ResponseBody
    public JSONResponse updateUser(@RequestParam("param")String param){
        System.out.println("param = " + param);
        JSONObject obj = JSONObject.parseObject(param);
        long id = obj.getLong("id");
        String name = obj.getString("name");
        String password = obj.getString("password");
        User user = new User(id,name,password);
        try {
            userService.update(user);
        }catch (Exception e){
            return JSONResponse.error("更新失败。");
        }
        return JSONResponse.success(user);
    }
}
