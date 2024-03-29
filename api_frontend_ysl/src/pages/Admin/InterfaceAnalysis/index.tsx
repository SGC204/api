import React, {useEffect, useState} from "react";
import {PageContainer} from "@ant-design/pro-components";
import ReactECharts from 'echarts-for-react';
import {listTopInterfaceInvokeInfoUsingGET} from "@/services/api_backend_ysl/analysisController";
import {message} from "antd";

/**
 * 接口分析
 * @constructor
 */
const InterfaceAnalysis: React.FC = () => {
  const [data, setData] = useState<API.InterfaceInfoVO[]>();
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    try {
      listTopInterfaceInvokeInfoUsingGET().then(res => {
        if (res.data) {
          setData(res.data);
        }
      })
    } catch (error: any) {
      message.error(error.message);
    }
  }, []);

  const charData = data?.map(item => {
    return {
      value: item.totalNum,
      name: item.name,
    }
  })

  const option = {
    title: {
      text: '调用次数最多的接口TOP3',
      left: 'center'
    },
    tooltip: {
      trigger: 'item'
    },
    legend: {
      orient: 'vertical',
      left: 'left'
    },
    series: [
      {
        name: 'Access From',
        type: 'pie',
        radius: '50%',
        data: charData,
        emphasis: {
          itemStyle: {
            shadowBlur: 10,
            shadowOffsetX: 0,
            shadowColor: 'rgba(0, 0, 0, 0.5)'
          }
        }
      }
    ]
  };

  return (
    <PageContainer>
      <ReactECharts loadingOption={{
        showLoading: loading
      }} option={option} />
    </PageContainer>
  );
};
export default InterfaceAnalysis;
