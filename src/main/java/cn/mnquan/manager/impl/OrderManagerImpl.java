package cn.mnquan.manager.impl;

import cn.mnquan.commons.Contents;
import cn.mnquan.manager.IOrderManager;
import cn.mnquan.manager.ITaobaoApiManager;
import cn.mnquan.mapper.TbMnOrderMapper;
import cn.mnquan.model.*;
import cn.mnquan.utils.DateUtil;
import cn.mnquan.utils.HttpClientUtils;
import com.github.pagehelper.PageHelper;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.taobao.api.ApiException;
import com.taobao.api.response.TbkItemInfoGetResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * 订单管理
 * </p>
 * User: wangkecheng Date: 2018/10/19
 */
@Service
@Slf4j
public class OrderManagerImpl implements IOrderManager{

    @Autowired
    private TbMnOrderMapper tbMnOrderMapper;
    @Autowired
    private ITaobaoApiManager taobaoApiManager;

    /**
     * 定时任务定时查询订单
     */
    public void queryOrderByTask() throws ApiException {
        String dateTime = DateUtil.getTenBeforeDate();
        /*String dateTime = "2018-10-21 01:30:00";*/
        String tenBefore = null;
        try {
            tenBefore = URLEncoder.encode(dateTime,"utf8");
        } catch (UnsupportedEncodingException e) {
            log.error("urlencoder编码失败，msg:{}",e.getMessage());
            log.error(e.getMessage(),e);
        }
        HttpClientResponse response =  HttpClientUtils.get(Contents.HM_URL+"?appkey="+Contents.HM_APPKEY+"&appsecret="+Contents.HM_APPSECRET+"&sid=3657&start_time="+tenBefore+"&span=600");

        log.info("自动查询订单,resp:{}",response);
        String json = response.getResponseContent();
        if("[]".equals(json)){
            return;
        }
        Gson gson = new Gson();
        java.lang.reflect.Type type = new TypeToken<JsonBean>(){}.getType();
        JsonBean jsonBean = gson.fromJson(response.getResponseContent(), type);
        List<TbMnOrderDo> orderDos = jsonBean.getMnOrderDos();
        for(TbMnOrderDo orderDo : orderDos){
            //判断表内是否存在，如果存在则更新
            TbMnOrderDoExample example = new TbMnOrderDoExample();
            example.createCriteria().andTradeIdEqualTo(orderDo.getTradeId());

            List<TbMnOrderDo> tbMnOrderDos = tbMnOrderMapper.selectByExample(example);
            if(null != tbMnOrderDos && tbMnOrderDos.size() > 0){
                tbMnOrderMapper.updateByExampleSelective(orderDo,example);
                continue;
            }

            //第一次插入时需要查出商品图片链接
            TbkItemInfoGetResponse.NTbkItem item = taobaoApiManager.queryProductItem(String.valueOf(orderDo.getNumIid()));
            if(null != item){
                orderDo.setPictUrl(item.getPictUrl());
            }
            tbMnOrderMapper.insertSelective(orderDo);
        }
    }

    /**
     * 查询所有的订单
     * @param tbMnOrderDo
     * @param adzoneIds
     * @return
     */
    public List<TbMnOrderDo> queryOrderByStatus(TbMnOrderDo tbMnOrderDo,List<String> adzoneIds) {
        PageHelper.startPage(tbMnOrderDo.getPageNo(),tbMnOrderDo.getPageSize());
        TbMnOrderDoExample example = new TbMnOrderDoExample();
        TbMnOrderDoExample.Criteria criteria = example.createCriteria();
        if(!"1".equals(String.valueOf(tbMnOrderDo.getTkStatus()))){
            if("14".equals(String.valueOf(tbMnOrderDo.getTkStatus()))){
                List<Short> values = new ArrayList<Short>();
                values.add((short)12);
                values.add((short)14);
                criteria.andTkStatusIn(values);
            }else {
                criteria.andTkStatusEqualTo(tbMnOrderDo.getTkStatus());
            }
        }
        criteria.andAdzoneIdIn(adzoneIds);
        example.setOrderByClause("create_time desc");

        List<TbMnOrderDo> orderDos = tbMnOrderMapper.selectByExample(example);
        for(TbMnOrderDo orderDo : orderDos){//对预估收入进行结算
            if(tbMnOrderDo.getAdzoneId().equals(orderDo.getAdzoneId())){//说明是代理本身的订单，则预估收入是原有的55%
                double temp = Double.valueOf(orderDo.getPubSharePreFee());
                BigDecimal bigDecimal = new BigDecimal(temp).multiply(new BigDecimal(55)).divide(new BigDecimal(100));
                double pubSharePreeFee = bigDecimal.setScale(2, BigDecimal.ROUND_DOWN).doubleValue();
                orderDo.setPubSharePreFee(String.valueOf(pubSharePreeFee));
            }else{//团队预估收入的5%
                double temp = Double.valueOf(orderDo.getPubSharePreFee());
                BigDecimal bigDecimal = new BigDecimal(temp).multiply(new BigDecimal(5)).divide(new BigDecimal(100));
                double pubSharePreeFee = bigDecimal.setScale(2, BigDecimal.ROUND_DOWN).doubleValue();
                orderDo.setPubSharePreFee(String.valueOf(pubSharePreeFee));
            }
        }
        return orderDos;
    }
    /**
     *获取用户自己的累计金额
     * @param tbMnUserDo
     * @return
     */
    public double getownAmt(TbMnUserDo tbMnUserDo) {
        return tbMnOrderMapper.selectOwmAmt(tbMnUserDo);
    }

    /**
     * 获取用户团队的累计金额
     * @param userDos
     * @return
     */
    public double getTeadAmt(List<TbMnUserDo> userDos) {
        return tbMnOrderMapper.selectTeamAmt(userDos);
    }

    /**
     *获取用户自己的待结算金额
     * @param tbMnUserDo
     * @return
     */
    public double getdaiOwnAmt(TbMnUserDo tbMnUserDo) {
        return tbMnOrderMapper.selectDaiOwmAmt(tbMnUserDo);
    }

    /**
     * 获取用户团队的待结算金额
     * @param userDos
     * @return
     */
    public double getDaiTeadAmt(List<TbMnUserDo> userDos) {
        return tbMnOrderMapper.selectDaiTeamAmt(userDos);
    }


    public static void main(String[] args) {
        double pubSharePreeFee = Double.valueOf("0.52");
        BigDecimal b = new BigDecimal(pubSharePreeFee).multiply(new BigDecimal(55)).divide(new BigDecimal(100));
        double df = b.setScale(2, BigDecimal.ROUND_DOWN).doubleValue();
        System.out.println(df);
    }
}
