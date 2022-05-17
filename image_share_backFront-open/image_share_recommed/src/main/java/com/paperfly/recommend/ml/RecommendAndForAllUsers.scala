package com.paperfly.recommend.ml

import com.paperfly.recommend.sql.RecommendToDataBase
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.spark.SparkContext
import org.apache.spark.ml.evaluation.RegressionEvaluator
import org.apache.spark.ml.recommendation.{ALS, ALSModel}
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.{DataFrame, Dataset, SparkSession}
import org.apache.spark.streaming.dstream.InputDStream

import java.util.Date

class RecommendAndForAllUsers {
  def recommend(data: InputDStream[ConsumerRecord[String, String]]): Unit = {
    //TODO 0.准备环境
    val spark: SparkSession = SparkSession.builder().appName("BatchAnalysis").master("local[*]")
      .config("spark.sql.shuffle.partitions", "20") //本次测试时将分区数设置小一点,实际开发中可以根据集群规模调整大小,默认200
      .getOrCreate()
    val sc: SparkContext = spark.sparkContext
    sc.setLogLevel("WARN")
    import org.apache.spark.sql.functions._
    import spark.implicits._ //split函数为SQL函数

    //TODO 1.加载数据并处理
    data.foreachRDD(rdd => {
      if (!rdd.isEmpty()) {
        //val frame: DataFrame =
        val ratingDF: DataFrame = rdd.map(line => {
          val rd = line.value()
          val arr: Array[String] = rd.split(",")
          //          println("line.value():"+line.value())
          (arr(0).toLong, arr(1).toLong, arr(2).toFloat)
        }).toDF("user_id", "post_id", "score")

        val Array(trainSet, testSet) = ratingDF.randomSplit(Array(0.7, 0.3)) //按照7:3划分训练集和测试集
        //TODO 2.构建ALS推荐算法模型并训练
        val als: ALS = new ALS()
          .setUserCol("user_id") //设置用户id是哪一列
          .setItemCol("post_id") //设置产品id是哪一列
          .setRatingCol("score") //设置评分列
          .setRank(6) //是模型中潜在因子（latent factors）的数量
          .setMaxIter(5) //最大迭代次数
          .setAlpha(1.0) //迭代步长
          .setRegParam(0.05) //是ALS的正则化参数（默认为1.0）。
          .setNonnegative(true) //决定是否对最小二乘法使用非负的限制（默认为false）

        //使用训练集训练模型
        val model: ALSModel = als.fit(trainSet)
        //得到预测评分的数据集
        println("数据总数="+ratingDF.count(),"训练数据个数="+(ratingDF.count()*0.8),"测试数据个数="+(ratingDF.count()*0.2),new Date())
        // 通过计算均方根误差RMSE(Root Mean Squared Error)对测试数据集评估模型
        // 注意下面使用冷启动策略drop，确保不会有NaN评估指标
        model.setColdStartStrategy("drop")
        val predictionsExplicit: DataFrame = model.transform(testSet)
        predictionsExplicit.show()

        //计算模型方差，数值越小预测越准确
        val evaluator = new RegressionEvaluator().setMetricName("rmse").setLabelCol("score").setPredictionCol("prediction")
        val rmseExplicit = evaluator.evaluate(predictionsExplicit)
        //打印计算得出的方差
        println(s"Explicit:Root-mean-square error = $rmseExplicit")


       /* //TODO 3.给用户做推荐
        val result1: DataFrame = model.recommendForAllUsers(5) //给所有用户推荐5部电影
        val dataBase = new RecommendToDataBase()
        //把推荐数据保存到数据库中
        dataBase.saveRecommendToUsers(result1)*/
      }
    })
  }


}
