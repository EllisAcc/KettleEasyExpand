/**
* Project Name:KettleUtil
* Date:2016年6月28日
* Copyright (c) 2016, jingma All Rights Reserved.
*/

package cn.benma666.km.job;

import org.quartz.DisallowConcurrentExecution;
import org.quartz.PersistJobDataAfterExecution;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

/**
 * kettle作业运行器 <br/>
 * 支持配置kettle作业一个或多个<br/>
 * date: 2016年6月28日 <br/>
 * @author jingma
 * @version 
 */
@PersistJobDataAfterExecution
@DisallowConcurrentExecution
public class KettleJobRunner extends AbsJob {
    
    ///////////配置参数//////////////////////
    private static final String JOBID_LIST = "作业id列表";
    /**
     * Creates a new instance of GenerateDataBill.
     */
    public KettleJobRunner() {
    }

    /**
    * 
    * @throws Exception 
     * @see cn.benma666.km.job.AbsJob#process(org.quartz.JobExecutionContext)
    */
    @Override
    protected void process() throws Exception {
        JSONArray jobIdList = configInfo.getJSONArray(JOBID_LIST);
        String sql = "select * from r_job j where id_job=?";
        //当前就是依次运行，将来根据需要可以考虑其他运行方式
        for(Integer jobId:jobIdList.toArray(new Integer[]{})){
            JSONObject jobJson = JobManager.kettledb.findFirst(sql, jobId);
            if(jobJson != null){
                try {
                    //此处存在一个作业被多处调用的可能，下一层控制了同一个作业同时只能运行一个，可能造成混乱
                    //所以建议一个作业不要多次使用，只能暂时只能靠自觉，瞎搞自己该遭
                    info("开始执行作业："+jobJson.getString("name"));
                    JobManager.startJob(jobJson);
                    JobManager.getJob(jobId).join();
                    info("结束执行作业："+jobJson.getString("name"));
                } catch (Exception e) {
                    error("作业执行失败："+jobJson.getString("name"), e);
                    break;
                }
            }else{
                error("作业不存在："+jobId);
            }
        }
    }

    public String getDefaultConfigInfo() throws Exception {
        JSONObject params = new JSONObject();
        params.put(JOBID_LIST, new JSONArray());
        return JSON.toJSONString(params, true);
    }

    
}
