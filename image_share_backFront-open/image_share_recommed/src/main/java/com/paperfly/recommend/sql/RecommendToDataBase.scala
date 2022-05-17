package com.paperfly.recommend.sql

import com.paperfly.recommend.utils.SnowFlakeGenerateIdWorker
import org.apache.spark.sql.{DataFrame, Dataset, Row}
import org.apache.spark.sql.functions.{col, current_timestamp, explode, lit, monotonically_increasing_id, unix_timestamp}

/**
 * 保存数据到mysql
 */
class RecommendToDataBase {

  def saveRecommendToUsers(data: DataFrame):Unit = {
    val Df2 = data.withColumn("postIdAndScore", explode(col("recommendations")))
      .drop("recommendations")
      .withColumn("post_id", col("postIdAndScore")
        .getItem("post_id")
      ).withColumn("score", col("postIdAndScore")
      .getItem("rating"))
      .drop("postIdAndScore")
      .withColumn("type",lit(0))
//      .withColumn("id",lit(snowFlakeGenerateIdWorker.generateNextId())+monotonically_increasing_id())
      .withColumn("create_time",current_timestamp())
      .withColumn("update_time",current_timestamp())

    //过滤掉推荐评分低的
    val realDf2: Dataset[Row] = Df2.filter(col("score") >= 3.0)

    val url = "jdbc:mysql://linux1:3306/image_share?useUnicode=true&characterEncoding=UTF-8&useSSL=false&serverTimezone=GMT%2B8&allowMultiQueries=true"
    val table="recommended"
    val prop = new java.util.Properties
    prop.setProperty("user","root")
    prop.setProperty("password","123456")
    realDf2.write.mode("append").jdbc(url, table, prop)
  }
}
