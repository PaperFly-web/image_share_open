let moment = require('moment');
moment.locale("zh-cn");
export const DateUtil = {
    formatTimeToCalendar
}

function formatTimeToCalendar(time){
    let formatTime = moment(time)
    return formatTime.calendar()
}