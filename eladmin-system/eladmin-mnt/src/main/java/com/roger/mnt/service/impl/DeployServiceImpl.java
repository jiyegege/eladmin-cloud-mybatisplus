package com.roger.mnt.service.impl;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.roger.common.core.domain.base.PageInfo;
import com.roger.common.security.utils.QueryHelpMybatisPlus;
import com.roger.common.core.domain.base.impl.BaseServiceImpl;
import com.roger.common.core.exception.BadRequestException;
import com.roger.common.core.utils.ConvertUtil;
import com.roger.common.core.utils.FileUtil;
import com.roger.common.core.utils.PageUtil;
import com.roger.common.core.utils.SecurityUtils;
import com.roger.mnt.domain.*;
import com.roger.mnt.domain.dto.AppDto;
import com.roger.mnt.domain.dto.DeployDto;
import com.roger.mnt.domain.dto.ServerDto;
import com.roger.mnt.domain.query.DeployQueryParam;
import com.roger.mnt.mapper.DeployMapper;
import com.roger.mnt.mapper.DeploysServersMapper;
import com.roger.mnt.mapper.ServerMapper;
import com.roger.mnt.service.*;
import com.roger.mnt.util.ExecuteShellUtil;
import com.roger.mnt.util.ScpClientUtil;
import com.roger.mnt.websocket.MsgType;
import com.roger.mnt.websocket.SocketMsg;
import com.roger.mnt.websocket.WebSocketServer;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;

/**
* @author jinjin
* @date 2020-09-27
*/
@Slf4j
@Service
@AllArgsConstructor
// @CacheConfig(cacheNames = DeployService.CACHE_KEY)
@Transactional(propagation = Propagation.SUPPORTS, readOnly = true, rollbackFor = Exception.class)
public class DeployServiceImpl extends BaseServiceImpl<Deploy> implements DeployService {

    private final String FILE_SEPARATOR = "/";
    // private final RedisUtils redisUtils;
    private final DeployMapper deployMapper;
    private final ServerMapper serverMapper;
    private final AppService appService;
    private final ServerService serverService;
    private final DeployHistoryService deployHistoryService;
    private final DeploysServersService deploysServersService;
    private final DeploysServersMapper deploysServersMapper;

    /**
     * ????????????
     */
    private final Integer count = 30;

    @Override
    public PageInfo<DeployDto> queryAll(DeployQueryParam query, Pageable pageable) {
        IPage<Deploy> page = PageUtil.toMybatisPage(pageable);
        IPage<Deploy> pageList = deployMapper.selectPage(page, QueryHelpMybatisPlus.getPredicate(query));
        PageInfo<DeployDto> pi = ConvertUtil.convertPage(pageList, DeployDto.class);
        for (DeployDto dd: pi.getContent() ) {
            dd.setApp(appService.findById(dd.getAppId()));

            QueryWrapper<Server> wrapper = new QueryWrapper<>();
            wrapper.lambda().in(Server::getId, deploysServersService.queryServerIdByDeployId(dd.getId()));
            dd.setDeploys(new HashSet<>(ConvertUtil.convertList(serverMapper.selectList(wrapper), ServerDto.class)));
        }
        return pi;
    }

    @Override
    public List<DeployDto> queryAll(DeployQueryParam query){
        List<DeployDto> list = ConvertUtil.convertList(deployMapper.selectList(QueryHelpMybatisPlus.getPredicate(query)), DeployDto.class);
        for (DeployDto dd: list) {
            dd.setApp(appService.findById(dd.getAppId()));

            QueryWrapper<Server> wrapper = new QueryWrapper<>();
            wrapper.lambda().in(Server::getId, deploysServersService.queryServerIdByDeployId(dd.getId()));
            dd.setDeploys(new HashSet<>(ConvertUtil.convertList(serverMapper.selectList(wrapper), ServerDto.class)));
        }
        return list;
    }

    @Override
    public Deploy getById(Long id) {
        return getById(id);
    }

    @Override
    // @Cacheable(key = "'id:' + #p0")
    public DeployDto findById(Long id) {
        return ConvertUtil.convert(deployMapper.selectById(id), DeployDto.class);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean save(DeployDto resources) {
        Deploy deploy = ConvertUtil.convert(resources, Deploy.class);
        deploy.setAppId(resources.getApp().getId());
        int ret = deployMapper.insert(deploy);
        if (deploy.getId() != null) {
            deploysServersService.removeByDeployId(deploy.getId());
        }
        for (ServerDto server: resources.getDeploys()) {
            DeploysServers ds = new DeploysServers();
            ds.setDeployId(deploy.getId());
            ds.setServerId(server.getId());
            deploysServersMapper.insert(ds);
        }
        return ret > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateById(DeployDto resources){
        Deploy deploy = ConvertUtil.convert(resources, Deploy.class);
        deploy.setAppId(resources.getApp().getId());
        int ret = deployMapper.updateById(deploy);
        if (deploy.getId() != null) {
            deploysServersService.removeByDeployId(deploy.getId());
        }
        for (ServerDto server: resources.getDeploys()) {
            DeploysServers ds = new DeploysServers();
            ds.setDeployId(deploy.getId());
            ds.setServerId(server.getId());
            deploysServersMapper.insert(ds);
        }
        return ret > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean removeByIds(Set<Long> ids){
        // delCaches(ids);
        for (Long id: ids) {
            deploysServersService.removeByDeployId(id);
        }
        return deployMapper.deleteBatchIds(ids) > 0;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean removeById(Long id){
        Set<Long> set = new HashSet<>(1);
        set.add(id);
        return this.removeByIds(set);
    }

    /*
    private void delCaches(Long id) {
        redisUtils.delByKey(CACHE_KEY + "::id:", id);
    }

    private void delCaches(Set<Long> ids) {
        for (Long id: ids) {
            delCaches(id);
        }
    }*/

    @Override
    public void download(List<DeployDto> all, HttpServletResponse response) throws IOException {
      List<Map<String, Object>> list = new ArrayList<>();
      for (DeployDto deploy : all) {
        Map<String,Object> map = new LinkedHashMap<>();
              map.put("????????????", deploy.getApp().getId());
              map.put("?????????", deploy.getCreateBy());
              map.put("?????????", deploy.getUpdateBy());
                map.put(" createTime",  deploy.getCreateTime());
              map.put("????????????", deploy.getUpdateTime());
        list.add(map);
      }
      FileUtil.downloadExcel(list, response);
    }

    @Override
    public void deploy(String fileSavePath, Long id) {
        deployApp(fileSavePath, id);
    }

    /**
     * @param fileSavePath ????????????
     * @param id ID
     */
    private void deployApp(String fileSavePath, Long id) {

        DeployDto deploy = findById(id);
        if (deploy == null) {
            sendMsg("?????????????????????", MsgType.ERROR);
            throw new BadRequestException("?????????????????????");
        }
        AppDto app = deploy.getApp();
        if (app == null) {
            sendMsg("??????????????????????????????", MsgType.ERROR);
            throw new BadRequestException("??????????????????????????????");
        }
        int port = app.getPort();
        //??????????????????????????????
        String uploadPath = app.getUploadPath();
        StringBuilder sb = new StringBuilder();
        String msg;
        Set<ServerDto> deploys = deploy.getDeploys();
        for (ServerDto deployDTO : deploys) {
            String ip = deployDTO.getIp();
            ExecuteShellUtil executeShellUtil = getExecuteShellUtil(ip);
            //???????????????????????????
            boolean flag = checkFile(executeShellUtil, app);
            //?????????????????????????????????????????????
            executeShellUtil.execute("mkdir -p " + app.getUploadPath());
            executeShellUtil.execute("mkdir -p " + app.getBackupPath());
            executeShellUtil.execute("mkdir -p " + app.getDeployPath());
            //????????????
            msg = String.format("??????????????????:%s", ip);
            ScpClientUtil scpClientUtil = getScpClientUtil(ip);
            log.info(msg);
            sendMsg(msg, MsgType.INFO);
            msg = String.format("????????????????????????:%s<br>??????:%s???????????????...", ip, uploadPath);
            sendMsg(msg, MsgType.INFO);
            scpClientUtil.putFile(fileSavePath, uploadPath);
            if (flag) {
                sendMsg("??????????????????", MsgType.INFO);
                //????????????
                stopApp(port, executeShellUtil);
                sendMsg("??????????????????", MsgType.INFO);
                //????????????
                backupApp(executeShellUtil, ip, app.getDeployPath()+FILE_SEPARATOR, app.getName(), app.getBackupPath()+FILE_SEPARATOR, id);
            }
            sendMsg("????????????", MsgType.INFO);
            //????????????,???????????????
            String deployScript = app.getDeployScript();
            executeShellUtil.execute(deployScript);
            sleep(3);
            sendMsg("????????????????????????????????????????????????????????????????????????????????????", MsgType.INFO);
            int i  = 0;
            boolean result = false;
            // ??????????????????????????????????????????????????????????????????????????????30??????????????????????????????
            while (i++ < count){
                result = checkIsRunningStatus(port, executeShellUtil);
                if(result){
                    break;
                }
                // ??????6???
                sleep(6);
            }
            sb.append("?????????:").append(deployDTO.getName()).append("<br>??????:").append(app.getName());
            sendResultMsg(result, sb);
            executeShellUtil.close();
        }
    }

    private void sleep(int second) {
        try {
            Thread.sleep(second * 1000);
        } catch (InterruptedException e) {
            log.error(e.getMessage(),e);
        }
    }

    private void backupApp(ExecuteShellUtil executeShellUtil, String ip, String fileSavePath, String appName, String backupPath, Long id) {
        String deployDate = DateUtil.format(new Date(), DatePattern.PURE_DATETIME_PATTERN);
        StringBuilder sb = new StringBuilder();
        backupPath += appName + FILE_SEPARATOR + deployDate + "\n";
        sb.append("mkdir -p ").append(backupPath);
        sb.append("mv -f ").append(fileSavePath);
        sb.append(appName).append(" ").append(backupPath);
        log.info("??????????????????:" + sb.toString());
        executeShellUtil.execute(sb.toString());
        //??????????????????
        DeployHistory deployHistory = new DeployHistory();
        deployHistory.setAppName(appName);
        deployHistory.setDeployUser(SecurityUtils.getCurrentUsername());
        deployHistory.setIp(ip);
        deployHistory.setDeployId(id);
        deployHistoryService.save(deployHistory);
    }

    /**
     * ???App
     *
     * @param port ??????
     * @param executeShellUtil /
     */
    private void stopApp(int port, ExecuteShellUtil executeShellUtil) {
        //??????????????????
        executeShellUtil.execute(String.format("lsof -i :%d|grep -v \"PID\"|awk '{print \"kill -9\",$2}'|sh", port));

    }

    /**
     * ?????????????????????????????????
     *
     * @param port ??????
     * @param executeShellUtil /
     * @return true ????????????  false ????????????
     */
    private boolean checkIsRunningStatus(int port, ExecuteShellUtil executeShellUtil) {
        String result = executeShellUtil.executeForResult(String.format("fuser -n tcp %d", port));
        return result.indexOf("/tcp:")>0;
    }

    private void sendMsg(String msg, MsgType msgType) {
        try {
            WebSocketServer.sendInfo(new SocketMsg(msg, msgType), "deploy");
        } catch (IOException e) {
            log.error(e.getMessage(),e);
        }
    }

    @Override
    public String serverStatus(DeployDto resources) {
        Set<ServerDto> Servers = resources.getDeploys();
        AppDto app = resources.getApp();
        for (ServerDto Server : Servers) {
            StringBuilder sb = new StringBuilder();
            ExecuteShellUtil executeShellUtil = getExecuteShellUtil(Server.getIp());
            sb.append("?????????:").append(Server.getName()).append("<br>??????:").append(app.getName());
            boolean result = checkIsRunningStatus(app.getPort(), executeShellUtil);
            if (result) {
                sb.append("<br>????????????");
                sendMsg(sb.toString(), MsgType.INFO);
            } else {
                sb.append("<br>?????????!");
                sendMsg(sb.toString(), MsgType.ERROR);
            }
            log.info(sb.toString());
            executeShellUtil.close();
        }
        return "????????????";
    }

    private boolean checkFile(ExecuteShellUtil executeShellUtil, AppDto appDTO) {
        String result = executeShellUtil.executeForResult("find " + appDTO.getDeployPath() + " -name " + appDTO.getName());
        return result.indexOf(appDTO.getName())>0;
    }

    /**
     * ????????????
     * @param resources /
     * @return /
     */
    @Override
    public String startServer(DeployDto resources) {
        Set<ServerDto> deploys = resources.getDeploys();
        AppDto app = resources.getApp();
        for (ServerDto deploy : deploys) {
            StringBuilder sb = new StringBuilder();
            ExecuteShellUtil executeShellUtil = getExecuteShellUtil(deploy.getIp());
            //????????????????????????????????????????????????
            stopApp(app.getPort(), executeShellUtil);
            sb.append("?????????:").append(deploy.getName()).append("<br>??????:").append(app.getName());
            sendMsg("??????????????????", MsgType.INFO);
            executeShellUtil.execute(app.getStartScript());
            sleep(3);
            sendMsg("????????????????????????????????????????????????????????????????????????????????????", MsgType.INFO);
            int i  = 0;
            boolean result = false;
            // ??????????????????????????????????????????????????????????????????????????????30??????????????????????????????
            while (i++ < count){
                result = checkIsRunningStatus(app.getPort(), executeShellUtil);
                if(result){
                    break;
                }
                // ??????6???
                sleep(6);
            }
            sendResultMsg(result, sb);
            log.info(sb.toString());
            executeShellUtil.close();
        }
        return "????????????";
    }

    /**
     * ????????????
     * @param resources /
     * @return /
     */
    @Override
    public String stopServer(DeployDto resources) {
        Set<ServerDto> deploys = resources.getDeploys();
        AppDto app = resources.getApp();
        for (ServerDto deploy : deploys) {
            StringBuilder sb = new StringBuilder();
            ExecuteShellUtil executeShellUtil = getExecuteShellUtil(deploy.getIp());
            sb.append("?????????:").append(deploy.getName()).append("<br>??????:").append(app.getName());
            sendMsg("??????????????????", MsgType.INFO);
            //????????????
            stopApp(app.getPort(), executeShellUtil);
            sleep(1);
            boolean result = checkIsRunningStatus(app.getPort(), executeShellUtil);
            if (result) {
                sb.append("<br>????????????!");
                sendMsg(sb.toString(), MsgType.ERROR);
            } else {
                sb.append("<br>????????????!");
                sendMsg(sb.toString(), MsgType.INFO);
            }
            log.info(sb.toString());
            executeShellUtil.close();
        }
        return "????????????";
    }

    @Override
    public String serverReduction(DeployHistory resources) {
        Long deployId = resources.getDeployId();
        Deploy deployInfo = deployMapper.selectById(deployId);
        String deployDate = DateUtil.format(resources.getDeployDate(), DatePattern.PURE_DATETIME_PATTERN);
        App app = appService.getById(deployInfo.getAppId());
        if (app == null) {
            sendMsg("????????????????????????" + resources.getAppName(), MsgType.ERROR);
            throw new BadRequestException("????????????????????????" + resources.getAppName());
        }
        String backupPath = app.getBackupPath()+FILE_SEPARATOR;
        backupPath += resources.getAppName() + FILE_SEPARATOR + deployDate;
        //??????????????????????????????
        String deployPath = app.getDeployPath();
        String ip = resources.getIp();
        ExecuteShellUtil executeShellUtil = getExecuteShellUtil(ip);
        String msg;

        msg = String.format("??????????????????:%s", ip);
        log.info(msg);
        sendMsg(msg, MsgType.INFO);
        sendMsg("??????????????????", MsgType.INFO);
        //????????????
        stopApp(app.getPort(), executeShellUtil);
        //??????????????????
        sendMsg("????????????", MsgType.INFO);
        executeShellUtil.execute("rm -rf " + deployPath + FILE_SEPARATOR + resources.getAppName());
        //????????????
        sendMsg("????????????", MsgType.INFO);
        executeShellUtil.execute("cp -r " + backupPath + "/. " + deployPath);
        sendMsg("????????????", MsgType.INFO);
        executeShellUtil.execute(app.getStartScript());
        sendMsg("????????????????????????????????????????????????????????????????????????????????????", MsgType.INFO);
        int i  = 0;
        boolean result = false;
        // ??????????????????????????????????????????????????????????????????????????????30??????????????????????????????
        while (i++ < count){
            result = checkIsRunningStatus(app.getPort(), executeShellUtil);
            if(result){
                break;
            }
            // ??????6???
            sleep(6);
        }
        StringBuilder sb = new StringBuilder();
        sb.append("?????????:").append(ip).append("<br>??????:").append(resources.getAppName());
        sendResultMsg(result, sb);
        executeShellUtil.close();
        return "";
    }

    private ExecuteShellUtil getExecuteShellUtil(String ip) {
        ServerDto ServerDto = serverService.findByIp(ip);
        if (ServerDto == null) {
            sendMsg("IP?????????????????????????????????" + ip, MsgType.ERROR);
            throw new BadRequestException("IP?????????????????????????????????" + ip);
        }
        return new ExecuteShellUtil(ip, ServerDto.getAccount(), ServerDto.getPassword(),ServerDto.getPort());
    }

    private ScpClientUtil getScpClientUtil(String ip) {
        ServerDto ServerDto = serverService.findByIp(ip);
        if (ServerDto == null) {
            sendMsg("IP?????????????????????????????????" + ip, MsgType.ERROR);
            throw new BadRequestException("IP?????????????????????????????????" + ip);
        }
        return ScpClientUtil.getInstance(ip, ServerDto.getPort(), ServerDto.getAccount(), ServerDto.getPassword());
    }

    private void sendResultMsg(boolean result, StringBuilder sb) {
        if (result) {
            sb.append("<br>????????????!");
            sendMsg(sb.toString(), MsgType.INFO);
        } else {
            sb.append("<br>????????????!");
            sendMsg(sb.toString(), MsgType.ERROR);
        }
    }

}
