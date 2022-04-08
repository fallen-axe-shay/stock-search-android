let pathname = window.location.pathname.split("/");
let ticker = pathname[pathname.length-1];

$.ajax({
    type : "GET",
    url  : `https://my-csci-hw-9-backend.wl.r.appspot.com/api/getCompanyRecommendationTrends/${ticker}`,
    async: true,
    success: (response) => {
        showHighCharts(response)
    },
    error: (error) => {
    }
});

function showHighCharts(response) {

    let categories = response.map((data) => data['period']);

    let data = [response.map((data) => data['strongBuy']), response.map((data) => data['buy']), response.map((data) => data['hold']), response.map((data) => data['sell']), response.map((data) => data['strongSell'])];


    // create the chart
    Highcharts.chart('container', {
        chart: {
            type: 'column'
          },
          title: {
            text: 'Recommendation Trends'
          },
          xAxis: {
            categories: categories
          },
          tooltip: {
              split: false
          },
          rangeSelector: {
              enabled: false
          },
          navigator: {
              enabled: false
          },
          yAxis: {
            min: 0,
            title: {
              text: '#Analysis'
            },
            stackLabels: {
              enabled: true,
            }
          },
          tooltip: {
            formatter: function () {
                return '<b>' + this.x + '</b><br/>' +
                    this.series.name + ': ' + this.y + '<br/>' +
                    'Total: ' + this.point.stackTotal;
            }
            },
          legend: {
            align: 'center',
            verticalAlign: 'bottom',
            backgroundColor: 'white',
            borderColor: '#d1d1d1',
            borderWidth: 1
          },
          plotOptions: {
            column: {
              stacking: 'normal',
              dataLabels: {
                enabled: true
              }
            }
          },
          series: [{
            name: 'Strong Buy',
            color: '#176f37',
            data: data[0]
          }, {
            name: 'Buy',
            color: '#1db954',
            data: data[1]
          }, {
            name: 'Hold',
            color: '#b98b1d',
            data: data[2]
          }, {
            name: 'Sell',
            color: '#f45e5e',
            data: data[3]
          },
          {
            name: 'Strong Sell',
            color: '#813131',
            data: data[4]
          }]
    });
}