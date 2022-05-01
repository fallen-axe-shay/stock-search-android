let pathname = window.location.pathname.split("/");
let ticker = pathname[pathname.length-1];

$.ajax({
    type : "GET",
    url  : `https://my-csci-hw-9-backend.wl.r.appspot.com/api/getCompanyEarnings/${ticker}`,
    async: true,
    success: (response) => {
        showHighCharts(response)
    },
    error: (error) => {
    }
});

function round(num) {
  var m = Number((Math.abs(num) * 100).toPrecision(15));
  return Math.round(m) / 100 * Math.sign(num);
}

function showHighCharts(response) {

    let actualData = response.map((item) => item['actual']==null ? 0 : item['actual']);

    let estimateData = response.map((item) => item['estimate']==null ? 0 : item['estimate']);

    let categories = response.map((item) => `${item['period']}<br>Surprise: ${item['surprise']==null ? 0.00 : round(item['surprise'])}`);


    // create the chart
    Highcharts.chart('container', {
        chart: {
          type: 'spline'
        },
        title: {
          text: 'Historical EPS Surprises'
        },
        xAxis: {
          maxPadding: 0.05,
          showLastLabel: true,
          categories: categories
        },
        yAxis: {
          title: {
            text: 'Quarterly EPS'
          },
          lineWidth: 2
        },
        legend: {
          enabled: true
        },
        tooltip: {
          split: false,
          shared: true
        },
        series: [{
          type: 'spline',
          name: 'Actual',
          data: actualData,
          showInLegend: true
        },{
          type: 'spline',
          name: 'Estimate',
          data: estimateData,
          showInLegend: true
        }]
      });
}