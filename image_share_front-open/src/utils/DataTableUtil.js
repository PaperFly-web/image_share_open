import {h} from "vue";
import {NEllipsis} from "naive-ui";
import MyNEllipsis from "../components/common/MyNEllipsis";
export const DataTableUtil = {
    renderTextTableData
}
//数据表格的文本数据渲染
function renderTextTableData(data) {
    return h(
        MyNEllipsis,
        {
            lineClamp: 1,
            style: "max-width: 100px",
            data:data
        }
    )
}