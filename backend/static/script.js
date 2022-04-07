let pathname = window.location.pathname.split("/");
let time = pathname[pathname.length-1];
let ticker = pathname[pathname.length-2];

$.ajax({
    type : "GET",
    url  : `https://my-csci-hw-9-backend.wl.r.appspot.com/api/getCompanyHistoricalDataTwoYears/${ticker}/${time}`,
    async: true,
    success: (response) => {
        showHighCharts(response)
    },
    error: (error) => {
    }
});

function showHighCharts(data) {
    // split the data set into ohlc and volume
    var ohlc = [],
        volume = [],
        dataLength = data['c'].length,
        // set the allowed units for data grouping
        groupingUnits = [[
            'week',                         // unit name
            [1]                             // allowed multiples
        ], [
            'month',
            [1, 2, 3, 4, 6]
        ]],

        i = 0;

    for (i; i < dataLength; i += 1) {
        ohlc.push([
            (data['t'][i] * 1000) - 7*60*60*1000, // the date
            data['o'][i], // open
            data['h'][i], // high
            data['l'][i], // low
            data['c'][i] // close
        ]);

        volume.push([
            (data['t'][i] * 1000) - 7*60*60*1000, // the date
            data['v'][i] // the volume
        ]);
    }


    // create the chart
    Highcharts.stockChart('container', {

        exporting: {
            enabled: false
        },

        rangeSelector: {
            selected: 2
        },

        title: {
            text: `${ticker} Historical`
        },

        subtitle: {
            text: 'With SMA and Volume by Price technical indicators'
        },

        yAxis: [{
            startOnTick: false,
            endOnTick: false,
            labels: {
                align: 'right',
                x: -3
            },
            title: {
                text: 'OHLC'
            },
            height: '60%',
            lineWidth: 2,
            resize: {
                enabled: true
            }
        }, {
            labels: {
                align: 'right',
                x: -3
            },
            title: {
                text: 'Volume'
            },
            top: '65%',
            height: '35%',
            offset: 0,
            lineWidth: 2
        }],

        xAxis: {
            type: "datetime"
        },

        tooltip: {
            split: true
        },

        plotOptions: {
            series: {
                dataGrouping: {
                    units: groupingUnits
                }
            }
        },

        series: [{
            type: 'candlestick',
            name: ticker,
            id: 'linkID',
            zIndex: 2,
            data: ohlc
        }, {
            type: 'column',
            name: 'Volume',
            id: 'volume',
            data: volume,
            yAxis: 1
        }, {
            type: 'vbp',
            linkedTo: 'linkID',
            params: {
                volumeSeriesID: 'volume'
            },
            dataLabels: {
                enabled: false
            },
            zoneLines: {
                enabled: false
            }
        }, {
            type: 'sma',
            linkedTo: 'linkID',
            zIndex: 1,
            marker: {
                enabled: false
            }
        }]
    });
}