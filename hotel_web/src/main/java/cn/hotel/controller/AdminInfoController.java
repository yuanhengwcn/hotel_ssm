package cn.hotel.controller;
import cn.hotel.controller.utils.JsonModel;
import cn.hotel.entity.AdminDto;
import cn.hotel.entity.Enum.SysResponse;
import cn.hotel.entity.model.AdminInfoRequest;
import cn.hotel.entity.model.AdminInfoResponse;
import cn.hotel.entity.model.PagerModel;
import cn.hotel.service.AdminInfoService;
import cn.hotel.service.utils.DateUtils;
import cn.hotel.service.utils.RestModel;
import com.alibaba.fastjson.JSON;
import com.google.common.io.Files;
import org.apache.commons.codec.Charsets;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.swing.filechooser.FileSystemView;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.*;


@Controller
public class AdminInfoController {
    private static final Logger logger = LoggerFactory.getLogger(AdminInfoController.class);

    @Autowired
    private AdminInfoService adminInfoService;

    //跳转到管理员界面
    @RequestMapping(value = "/htm/user/adminInfo.action")
    public String adminInfo(HttpServletRequest request){
        return  "adminInfo";
    }


    //管理员信息列表展示
    @RequestMapping(value = "/htm/adminInfoList.action",method = RequestMethod.GET)
    @ResponseBody
    public PagerModel<List<AdminInfoResponse>> getCustomer(HttpServletRequest request) {
        PagerModel <List <AdminInfoResponse>> pager = new PagerModel <>();
        List <AdminInfoResponse> list = new ArrayList<>();
        Long count = 0L;
        AdminInfoRequest adminInfoRequest = getSearchParam(request);

        logger.info("用户信息查询参数 param={}", JSON.toJSONString(adminInfoRequest));
        RestModel restModel = adminInfoService.findAllAdminInfo(adminInfoRequest);
        logger.info("用户信息查询返回结果 param={}", JSON.toJSONString(restModel));
        List data = (List) restModel.getData();
        if(restModel.getCode().equals(SysResponse.RECORD_CODE.RESPONSE_SUCCESS.get().toString())){

            logger.info("用户信息查询参数 param={}", JSON.toJSONString(adminInfoRequest));
            restModel = adminInfoService.countAdminInfoRecord(adminInfoRequest);
            logger.info("用户信息查询返回结果 param={}", JSON.toJSONString(restModel));
            count = Long.valueOf(restModel.getData().toString());
            pager.setTotal(count);
            pager.setPageData(data);
            return pager;
        }
        pager.setTotal(count);
        pager.setPageData(data);
        return pager;
    }



    //添加记录
    @RequestMapping(value = "/htm/adminInfoAdd.action",method = RequestMethod.POST)
    @ResponseBody
    public JsonModel adminUserAdd(HttpServletRequest request,@RequestParam(value = "adminName",required = true) String adminName,
                                                             @RequestParam(value = "adminPwd",required = true) String adminPwd,
                                                             @RequestParam(value = "adminRealName",required = true) String adminRealName,
                                                             @RequestParam(value = "adminSex",required = true) String adminSex,
                                                             @RequestParam(value = "adminBirthday",required = true) String adminBirthday,
                                                             @RequestParam(value = "adminNation",required = true) String adminNation,
                                                             @RequestParam(value = "adminIdCard",required = true) String adminIdCard,
                                                             @RequestParam(value = "adminMobile",required = true) String adminMobile,
                                                             @RequestParam(value = "adminAdress",required = true) String adminAdress,
                                                             @RequestParam(value = "adminIdType",required = true) String adminIdType,
                                                             @RequestParam(value = "adminIspostion",required = true)String adminIsPostion){
        JsonModel jsonModel = new JsonModel();
        if(StringUtils.isEmpty(adminName)
            || StringUtils.isEmpty(adminPwd)
            || StringUtils.isEmpty(adminRealName)
            || StringUtils.isEmpty(adminSex)
            || StringUtils.isEmpty(adminBirthday)
            || StringUtils.isEmpty(adminNation)
            || StringUtils.isEmpty(adminIdCard)
            || StringUtils.isEmpty(adminMobile)
            || StringUtils.isEmpty(adminAdress)
            || StringUtils.isEmpty(adminIdType)
            || StringUtils.isEmpty(adminIsPostion)){
            jsonModel.setStatus(false);
            jsonModel.setMessage("所传入的参数不能为空！");
            return jsonModel;
        }

        //数据封装
        AdminDto adminRequest = converData(adminName, adminPwd, adminRealName, adminSex, adminBirthday, adminNation,
                adminIdCard, adminMobile, adminAdress, adminIdType, adminIsPostion);
        logger.info("新增管理员信息传入参数 param={}",JSON.toJSONString(adminRequest));
        RestModel restModel = adminInfoService.adminInfoAdd(adminRequest);
        if(RestModel.CODE_SUCCESS.toString().equals(restModel.getCode().toString())){
            jsonModel.setStatus(true);
            jsonModel.setMessage("添加成功");
            jsonModel.setResult(restModel.getData());
            return jsonModel;
        }else{
            jsonModel.setStatus(false);
            jsonModel.setMessage("添加失败");
            return jsonModel;
        }

    }

    //修改记录
    @RequestMapping(value = "/htm/adminInfoModify.action",method = RequestMethod.POST)
    @ResponseBody
    public JsonModel adminUserAdd(HttpServletRequest request){
        JsonModel jsonModel = new JsonModel();
        AdminDto adminRequest = getModifySearchParam(request);
        if(adminRequest ==null){
            jsonModel.setStatus(false);
            jsonModel.setMessage("所有参数不能为空！");
            return  jsonModel;
        }
        logger.info("修改管理员信息传入的参数 param={}",JSON.toJSONString(adminRequest));
        RestModel restModel = adminInfoService.updateAdminInfo(adminRequest);
        if(RestModel.CODE_SUCCESS.toString().equals(restModel.getCode().toString())){
            jsonModel.setStatus(true);
            jsonModel.setMessage("修改成功");
            jsonModel.setResult(restModel.getData());
            return jsonModel;
        }else{
            jsonModel.setStatus(false);
            jsonModel.setMessage("修改失败");
            return jsonModel;
        }

    }

    public AdminDto getModifySearchParam(HttpServletRequest request){
        AdminDto adminDto = new AdminDto();
        String modifyAdminId = request.getParameter("modifyAdminId");
        if(StringUtils.isNotBlank(modifyAdminId)){
            adminDto.setAdminId(Long.valueOf(modifyAdminId));
        }
        String mobile = request.getParameter("modifyAdminMobile");
        if(StringUtils.isNotBlank(mobile)){
            adminDto.setAdminMobile(Long.valueOf(mobile));
        }
        String modifyAddAdminAddress = request.getParameter("modifyAddAdminAddress");
        if(StringUtils.isNotBlank(modifyAddAdminAddress)){
            adminDto.setAddress(modifyAddAdminAddress);
        }
        String modifyAdminIspostion = request.getParameter("modifyAdminIspostion");
        if(StringUtils.isNotBlank(modifyAdminIspostion)){
            adminDto.setAdminIsPostion(modifyAdminIspostion);
        }
        return adminDto;
    }


    public AdminDto converData(String adminName,String adminPwd,String adminRealName,String adminSex,String adminBirthday,String adminNation,
                               String adminIdCard,String adminMobile,String adminAdress,String adminIdType,String adminIsPostion){
        AdminDto adminInfoRequest = new AdminDto();
        adminInfoRequest.setAdminName(adminName);
        adminInfoRequest.setAdminPwd(adminPwd);
        adminInfoRequest.setAdminRealName(adminRealName);
        adminInfoRequest.setAdminSex(adminSex);
        adminInfoRequest.setAdminBirthday(adminBirthday);
        adminInfoRequest.setAdminNation(adminNation);
        adminInfoRequest.setAdminIdCard(Long.valueOf(adminIdCard));
        adminInfoRequest.setAdminMobile(Long.valueOf(adminMobile));
        adminInfoRequest.setAddress(adminAdress);
        adminInfoRequest.setAdminIdCardType(adminIdType);  //身份类型
        adminInfoRequest.setAdminIsPostion(adminIsPostion);

        return adminInfoRequest;
    }

    //导出数据文件
    @RequestMapping(value = "/htm/exportAdminUserInfoFile.action",method = RequestMethod.POST)
    @ResponseBody
    public JsonModel exportExcel(HttpServletRequest request, HttpServletResponse response){
        JsonModel jsonModel = new JsonModel();
        Long count = 0L;
        AdminInfoRequest adminInfoRequest = getSearchParam(request);
        logger.info("用户信息查询参数 param={}", JSON.toJSONString(adminInfoRequest));
        RestModel restModel = adminInfoService.findAllAdminInfo(adminInfoRequest);
        logger.info("用户信息查询返回结果 param={}", JSON.toJSONString(restModel));
        List<AdminDto> data = (List) restModel.getData();
        if(restModel.getCode().equals(SysResponse.RECORD_CODE.RESPONSE_SUCCESS.get().toString())){
            try{
                //应当前的时间戳定义导出文件的名称
                Long currentTimeMillis = System.currentTimeMillis();
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
                String newDate = simpleDateFormat.format(currentTimeMillis).toString().substring(0, 10);

                String fileName = String.valueOf(newDate+"管理员报表");
                //写文件头 Files是gua的架包
                String path = FileSystemView.getFileSystemView().getHomeDirectory().getPath();//这便是读取桌面路径的方法了

                File exportFile = new File(path +"//"+ fileName + ".xls");
                Files.append(new String(new byte[]{(byte)0xEF, (byte)0xBB, (byte)0xBF}, "UTF-8"), exportFile, Charsets.UTF_8);  //对文件添加BOM头后，用excel打开即可无乱码
                Files.append("管理员ID,管理员呢称,管理员密码,管理员真实姓名,民族,身份证,手机号,身份证类型,是否在岗,创建时间,修改时间\r\n", exportFile, Charsets.UTF_8);
                StringBuilder sb = new StringBuilder();
                for(AdminDto model : data){
                    sb.append(model.getAdminId()==null? "":model.getAdminId()).append(",");
                    sb.append(model.getAdminName()==null? "":model.getAdminName()).append(",");
                    sb.append(model.getAdminPwd()==null? "":model.getAdminPwd()).append(",");
                    sb.append(model.getAdminRealName()==null? "":model.getAdminRealName()).append(",");
                    sb.append(model.getAdminNation()==null?"":model.getAdminNation()).append(",");
                    sb.append(model.getAdminIdCard()==null? "":model.getAdminIdCard()).append(",");
                    sb.append(model.getAdminMobile()==null ? "":model.getAdminMobile()).append(",");
                    sb.append(showUserIdCardType(model.getAdminIdCardType()==null? "":model.getAdminIdCardType())).append(",");
                    sb.append(showIsPostion(model.getAdminIsPostion())).append(",");        //是否在岗
                    String createTime = simpleDateFormat.format(model.getCreateTime());
                    sb.append(createTime==null? "":createTime).append(",");
                    String modifyTime = simpleDateFormat.format(model.getModifyTime());
                    sb.append(modifyTime==null? "":modifyTime).append(",");
                    sb.append("\r\n");
                }
                Files.append(sb.toString(),exportFile, Charsets.UTF_8);
                InputStream input = new FileInputStream(exportFile);
                jsonModel.setMessage(input.toString());
                jsonModel.setStatus(true);
                jsonModel.setResult(data);
            }catch (Exception ex){
                logger.info("导出异常");
                jsonModel.setStatus(false);
                jsonModel.setMessage("导出异常");
            }
        }
        return jsonModel;
    }


    //数据格式话
    private  String showIsPostion(String value){
        if(value.equals("1")){
            return  "在岗";
        }else if(value.equals("0")){
            return  "离职";
        }else{
            return  value;
        }
    }

    private String  showUserIdCardType(String value) {
        if(value.equals("0")){
            return "身份证";
        }else if(value.equals("1")){
            return "护照";
        }else if(value.equals("2")){
            return "军官证";
        }else if(value.equals("3")){
            return "士兵证";
        }else if(value.equals("4")){
            return "回乡证";
        }else if(value.equals("5")){
            return "户口本";
        }else if(value.equals("6")){
            return "外国护照";
        }else if(value.equals("7")){
            return "台胞证";
        }else if(value.equals("8")){
            return "其他";
        }
        return value;

    }


    //删除记录
    @RequestMapping(value = "/htm/adminInfoDelete.action",method = RequestMethod.POST)
    @ResponseBody
    public JsonModel adminInfoDelete(HttpServletRequest request, @RequestParam(value = "adminId",required = true)String adminId,
                                                                 @RequestParam(value = "adminIsPostion",required = true) String adminIsPostion) {
        JsonModel jsonModel = new JsonModel();
        if(StringUtils.isBlank(adminId)
            ||StringUtils.isBlank(adminIsPostion)){
             jsonModel.setStatus(false);
             jsonModel.setMessage("所传参数不能为空！");
             return  jsonModel;
        }
        if(StringUtils.isBlank(adminIsPostion)&& Integer.valueOf(adminIsPostion)>0){
            jsonModel.setStatus(false);
            jsonModel.setMessage("非离职岗位不能删除");
            return jsonModel;
        }
        Map <String, Object> param = new HashMap <>();
        param.put("adminId",adminId);
        param.put("adminIsPostion",adminIsPostion);

        logger.info("删除离职管理员的信息传入参数 param={}", JSON.toJSONString(param));
        RestModel restModel = adminInfoService.deleteAdminInfoRecord(param);
        logger.info("用户信息查询返回结果 param={}", JSON.toJSONString(param));
        if(restModel.getCode().equals("200") && restModel.getData().toString().equals("1")){
            jsonModel.setStatus(true);
            jsonModel.setMessage("删除离职员工成功！");
            return jsonModel;
        }
        jsonModel.setStatus(false);
        jsonModel.setMessage("删除离职员工失败！");
        return jsonModel;
    }

    private AdminInfoRequest getSearchParam(HttpServletRequest request) {

        AdminInfoRequest adminInfoRequest = new AdminInfoRequest();
        String pageSize = request.getParameter("pageSize");
        if (StringUtils.isNotBlank(pageSize)) {
            adminInfoRequest.setPageSize(Long.valueOf(pageSize));
        } else {
            adminInfoRequest.setPageSize(Long.valueOf(50));
        }
        String pageNumber = request.getParameter("pageNumber");
        if (StringUtils.isNotBlank(pageNumber)) {
            if (Integer.valueOf(pageNumber) <= 1) {
                adminInfoRequest.setPageNumber(Long.valueOf(0));
            } else {
                adminInfoRequest.setPageNumber(Long.valueOf(pageNumber));
            }
        } else {
            adminInfoRequest.setPageNumber(Long.valueOf(0));
        }

        String searchAdminId = request.getParameter("searchAdminId");
        if(StringUtils.isNotBlank(searchAdminId)){
            adminInfoRequest.setAdminId(Long.valueOf(searchAdminId));
        }
        String mobile = request.getParameter("searchMobile");
        if(StringUtils.isNotBlank(mobile)){
            adminInfoRequest.setAdminMobile(Long.valueOf(mobile));
        }
        String createTime = request.getParameter("createTimeStart");
        if(StringUtils.isNotBlank(createTime)){
            adminInfoRequest.setCreateTimeStart(DateUtils.getLongByDateString(createTime));
        }
        String createTimeEnd = request.getParameter("crateTimeEnd");
        if(StringUtils.isNotBlank(createTimeEnd)){
            adminInfoRequest.setCreateTimeEnd(DateUtils.getLongByString(createTimeEnd+" 23:59:59"));
        }
        return adminInfoRequest;
    }

}
