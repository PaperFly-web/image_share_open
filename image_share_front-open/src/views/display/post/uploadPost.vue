<template>
  <Loading :msg="loading.msg" :key="loading.msg" v-if="loading.isShow"
           style="color: #CCCCCC ;font-family:'Microsoft soft';font-weight: 700"></Loading>
  <div class="post">
    <div class="postBody">
      <!--      选择图片-->
      <div v-if="postItemShow.selectImg" id="selectImg" class="postItem" @dragleave="fileDragleave"
           @dragover="fileDragover" @drop="fileDrop">
        <header>
          <span>创建新的帖子</span>
        </header>
        <article>
          <n-icon :color="dropIconColor" :size="100">
            <ImagesOutline/>
          </n-icon>
          <span id="dropTips">把照片拖放到这里</span>
          <input name="file" id="file" class="inputFile" @change="changePic" multiple="multiple"
                 accept="image/png,image/jpg,image/jpeg"
                 type="file">
          <label for="file">点击选择图片</label>
        </article>

      </div>
      <!--      编辑图片-->
      <div v-if="postItemShow.editImg" id="editImg" class="postItem">
        <header id="editImgHeader">

          <n-popconfirm
              negative-text="取消"
              positive-text="确定"
              @positive-click="editImgHeaderPreBtnClick"
          >
            <template #trigger>
              <n-button id="editImgHeaderBtn" type="info">
                放弃当前编辑
              </n-button>
            </template>
            如果退出，你所做的编辑不会保存。
          </n-popconfirm>
          <span>编辑图片</span>
          <n-button type="info" id="editImgHeaderBtn" @click="editImgHeaderBtnClick">
            下一步
          </n-button>
        </header>
        <section>
          <!--          v-for="imgBlob in imageBlobs"-->
          <div id="leftBarImg" ref="leftBarImgRef">
            <n-image
                v-for="(imgBlobUrl,index) in imageBlobsUrl"
                preview-disabled
                width="50"
                :src="imageBlobsUrl[index]"
                :key="imageBlobsUrl[index]"
                fallback-src="https://07akioni.oss-cn-beijing.aliyuncs.com/07akioni.jpeg"
                @click="editImgClick(index)"
            />
          </div>
          <div id="rightEditImg">
            <tui-image-editor canvasMaxHeight="500" canvasMaxWidth="750" @imgChange="tuiImageEditorImgChange"
                              :key="tuiImageEditor.key"
                              :url="tuiImageEditor.tuiImageEditorSrc" style="height: 100%"></tui-image-editor>
          </div>
        </section>

      </div>
      <!--      编辑帖子-->
      <div v-if="postItemShow.editPost" id="editPost" class="postItem">
        <header id="editPostHeader">
          <n-button type="info" class="editPostHeaderBtn" @click="editPostHeaderPreBtnClick">
            上一步
          </n-button>
          <span>编辑帖子</span>
          <n-button type="info" class="editPostHeaderBtn" @click="editPostHeaderAftBtnClick">
            发布帖子
          </n-button>
        </header>
        <section id="editImgSection">
          <div id="editPostImg" v-if="!mobileOrPC">
            <n-carousel class="carousel-img" show-arrow effect="card" :slides-per-view="1" :space-between="20"
                        :loop="false" draggable>

              <n-image
                  v-for="(imgBlobUrl,index) in imageBlobsUrl"
                  class="carousel-img"
                  object-fit="cover"
                  :src="imgBlobUrl"
              />
            </n-carousel>
            <n-progress type="line" :percentage="percentage" :show-indicator="false"/>
          </div>
          <div id="editPostContent">

            <div class="inputComment">
              <n-input
                  style="height: 100%"
                  v-model:value="post.originalContent"
                  type="textarea"
                  size="medium"
                  placeholder="说点什么吧"
                  maxlength="600"
                  show-count
                  clearable
                  :autosize="{ minRows: 20, maxRows: 22 }"
              />
              <emoji :disabled="post.isOpenComment===0" @emoji="setEmoji"></emoji>
            </div>

            <n-switch @update:value="placeSwitch" size="small">
              <template #checked>
                {{ post.place }}
              </template>
              <template #unchecked>
                获取地点
              </template>
            </n-switch>
            <n-switch @update:value="commentSwitch" size="small">
              <template #checked>
                打开评论
              </template>
              <template #unchecked>
                关闭评论
              </template>
            </n-switch>
            <n-dynamic-tags style="margin-top: 10px" :max="6" v-model:value="post.listTopic"/>
          </div>

        </section>
      </div>
    </div>
  </div>
  <footer>
    <div>
      <p>版权所有2013- 2022ImageShare图片分享</p>
    </div>
  </footer>
</template>

<script>
import {defineComponent, onBeforeUnmount, onMounted, reactive, ref} from 'vue'
import {useMessage, NUpload, NModal, NProgress, NCarousel, NCarouselItem, NDynamicTags, NPopconfirm} from 'naive-ui'
import {FileUtil} from "../../../utils/FileUtil";
import {RandomUtil} from "../../../utils/randomUtil";
import {HTTP} from "../../../network/request";
import axios from "axios";
import {UserInfoUtil} from "../../../utils/UserInfoUtil";
import tuiImageEditor from '../../../components/img/tuiImageEditor'
import {
  ImagesOutline
} from "@vicons/ionicons5";
import Loading from "../../../components/Loading";
import Emoji from "../../../components/common/emoji";
import router from "../../../router";


export default defineComponent({
  components: {
    Emoji,
    NModal, NUpload, NProgress, ImagesOutline, NCarousel, NCarouselItem, NDynamicTags, NPopconfirm,
    Loading, tuiImageEditor
  },
  setup() {
    const message = useMessage()
    let loading = reactive({
      isShow: false,
      msg: ""
    })
    let post = reactive({
      place: null,
      country: null,
      region: null,
      city: null,
      area: null,
      isp: null,
      originalContent: null,
      listImagesPath: [],
      listTopic: [],
      isOpenComment: 1,
    })

    let postItemShow = reactive({
      selectImg: true,
      editImg: false,
      editPost: false,
      toSelectImg() {
        postItemShow.selectImg = true
        postItemShow.editImg = false
        postItemShow.editPost = false
      },
      toEditImg() {
        postItemShow.selectImg = false
        postItemShow.editImg = true
        postItemShow.editPost = false
      },
      toEditPost() {
        postItemShow.selectImg = false
        postItemShow.editImg = false
        postItemShow.editPost = true
      },
    })


    //图片上传进度条
    let percentage = ref(0)
    let imageBlobs = reactive([]);
    let imageBlobsUrl = reactive([]);


    //点击选择文件
    async function changePic(e) {
      let evt = e || window.event;
      let files = evt.target.files;
      await handleImgFiles(files)
    }

    //拖拽文件时候图标显示变化
    let dropIconColor = ref(null)

    //拖拽文件时候高亮显示
    function fileDragover(e) {
      e.preventDefault()
      // console.log("fileDragover")
      dropIconColor.value = "#0971e8"
    }
    //拖拽文件时候取消高亮显示
    function fileDragleave(e) {
      e.preventDefault()
      // console.log("fileDragleave")
      dropIconColor.value = ""
    }

    async function fileDrop(e) {
      e.preventDefault()
      dropIconColor.value = ""
      const files = e.dataTransfer.files
      await handleImgFiles(files)
    }

    //处理获取到的图片文件
    async function handleImgFiles(files) {
      if (files.length > 9) {
        message.error("最多只能选择9张图片")
        return
      }
      loading.msg = "图片处理中"
      loading.isShow = true;
      let imageBlobsSize = imageBlobs.length
      for (let i = 0; i < imageBlobsSize; i++) {
        imageBlobs.pop()
        imageBlobsUrl.pop()
      }
      //将文件全部转换成blob对象
      await filesToBlobs(files, (bobs) => {
        // imageBlobs = res
        for (let blob of bobs) {
          imageBlobs.push(blob)
          imageBlobsUrl.push(FileUtil.blobToBlobUrl(blob))
        }
      })
      //图片处理完毕，关闭提示
      loading.isShow = false
      if (!FileUtil.isImg(imageBlobs)) {
        message.error("当前选中含有不是jpg,jpeg,png图片")
        return
      }
      //查看当前是不是手机端
      if(mobileOrPC.value){
        // console.log("mobileOrPC",mobileOrPC)
        postItemShow.toEditPost()
      }else {
        postItemShow.toEditImg()
      }
      //设置第一张图片为当前默认编辑图片
      tuiImageEditor.tuiImageEditorSrc = imageBlobsUrl[0]
    }

    //files  to  blobs
    async function filesToBlobs(files, cab) {
      let blobs = []
      //将文件全部转换成blob对象
      for (let file of files) {
        const readFileAsync = file => new Promise(resolve => {
          const reader = new FileReader();
          reader.onload = evt => resolve(evt.target.result)
          //不执行这个就加载不了文件，reader.onload就执行不了
          reader.readAsDataURL(file)
        }).then(res => {
          let imageBlob = FileUtil.base64ToBlob(res)
          blobs.push(imageBlob)
        })
        await readFileAsync(file)
      }
      //回调函数，把转换结果传回去
      cab(blobs)
    }

    let leftBarImgRef = ref(null)

    //编辑图片点击
    function editImgClick(index) {
      //设置当前显示的编辑图片
      tuiImageEditor.tuiImageEditorSrc = imageBlobsUrl[index]
      tuiImageEditor.key = index
      tuiImageEditor.currIndex = index
      // console.log("<<<<<<<<<<<<<<<<<<<tuiImageEditorSrc>>>>>>>>>>>>>>>>>>>>>",tuiImageEditorSrc.value)
    }

    let tuiImageEditor = reactive({
      tuiImageEditorSrc: "",
      currIndex: 0,
      key: 0
    })

    //当前编辑的图片
    function tuiImageEditorImgChange(imgBlob) {
      // console.log("tuiImageEditorImgChange", imgBlob, tuiImageEditor.currIndex)
      imageBlobs[tuiImageEditor.currIndex] = imgBlob
      imageBlobsUrl[tuiImageEditor.currIndex] = FileUtil.blobToBlobUrl(imgBlob)
    }

    //编辑图片下一步
    function editImgHeaderBtnClick() {
      postItemShow.selectImg = false;
      postItemShow.editImg = false;
      postItemShow.editPost = true;
    }

    //放弃当前编辑
    function editImgHeaderPreBtnClick() {
      postItemShow.selectImg = true;
      postItemShow.editImg = false;
      postItemShow.editPost = false;
      post.place = null
      post.area = null
      post.city = null
      post.country = null
      post.isp = null
      post.region = null
      post.listImagesPath = []
      post.listTopic = []
      post.originalContent = null
      post.isOpenComment = 1
    }

    //发布帖子
    function editPostHeaderAftBtnClick() {
      console.log("发布帖子了")
      loading.isShow = true
      loading.msg = "图片上传中"
      //先上传图片
      // 构建FormData
      let formData = new FormData();
      for (let blob of imageBlobs) {
        //注意：此处第3个参数最好传入一个带后缀名的文件名，否则很有可能被后台认为不是有效的图片文件
        //生成一个随机文件名
        let fileName = `${RandomUtil.uuid(20, 16)}.${FileUtil.getBlobTypeSuffix(blob)}`;
        formData.append("images", blob, fileName);
      }

      // 上传文件
      //TODO  测试结果，这个是可以的，千万不要删除！！！！！！！！！！！！！！！！！！！！！！！！！
      axios.post(`${HTTP.baseURL}/post/uploadPostImages`, formData, {
        // withCredentials:true,
        headers: {'Authorization': UserInfoUtil.getUserInfo().token,},
        onUploadProgress: ({loaded, total}) => {
          // onProgress({ percent: Math.ceil((loaded / total) * 100) })
          percentage.value = Math.ceil((loaded / total) * 100);
          // console.log(percentage.value)
        }
      }).then(res => {
        loading.msg = "帖子发送中"
        if (res.data.code === 0) {
          console.log(res.data.msg)
          post.listImagesPath = res.data.data
          //发布帖子
          HTTP.myHttp({
            url: "/post",
            method: "post",
            data: post
          }).then(res => {
            loading.isShow = false
            if (res.data.code === 0) {
              message.success(res.data.msg)
              router.push("/")
            } else {
              message.error(res.data.msg)
            }
          }).catch(err => {
            loading.isShow = false
          })
        } else {
          loading.isShow = false
          message.error(res.data.msg)
        }
      }).catch((error) => {
        loading.isShow = false
      })
    }

    //编辑帖子的上一步
    function editPostHeaderPreBtnClick() {
      if(mobileOrPC.value){
        editImgHeaderPreBtnClick()
      }else {
        postItemShow.selectImg = false;
        postItemShow.editImg = true;
        postItemShow.editPost = false;
        tuiImageEditor.tuiImageEditorSrc = imageBlobsUrl[tuiImageEditor.currIndex]
        console.log("来到编辑图片部分")
      }

    }

    function placeSwitch(b) {
      console.log("placeSwitch", b)
      if (b) {
        HTTP.GET("open/ip/currentLocation").then(res => {
          console.log(res.data)
          post.place = res.data.place
          post.area = res.data.area
          post.city = res.data.city
          post.country = res.data.country
          post.isp = res.data.isp
          post.region = res.data.region
        })
      } else {
        post.place = null
        post.area = null
        post.city = null
        post.country = null
        post.isp = null
        post.region = null
      }

    }

    function commentSwitch(b) {
      console.log("commentSwitch", b)
      if (b) {
        post.isOpenComment = 0
      } else {
        post.isOpenComment = 1
      }
    }

    //手机端还是PC端
    let mobileOrPC = ref(false)

    function loadScreenResize() {
      let width = document.body.offsetWidth;
      if (width > 700) {
        mobileOrPC.value = false
      } else {
        mobileOrPC.value = true
      }
    }


    function setEmoji(emoji){
      if(post.originalContent === null){
        post.originalContent =emoji
      }else {
        post.originalContent +=emoji
      }
    }

    onMounted(() => {
      loadScreenResize();
      window.addEventListener('resize', loadScreenResize)
    })


    onBeforeUnmount(() => {
      window.removeEventListener("resize", loadScreenResize)
    })

    return {
      changePic,
      tuiImageEditor,
      percentage,
      fileDragover,
      fileDrop,
      dropIconColor,
      fileDragleave,
      loading,
      post,
      imageBlobs,
      FileUtil,
      postItemShow,
      leftBarImgRef,
      editImgClick,
      tuiImageEditorImgChange,
      imageBlobsUrl,
      editImgHeaderBtnClick,
      editImgHeaderPreBtnClick,
      editPostHeaderAftBtnClick,
      editPostHeaderPreBtnClick,
      placeSwitch,
      commentSwitch,
      mobileOrPC,
      setEmoji
    }
  }
})
</script>

<style scoped>
.postBody {
  max-width: 1105px;
  min-height: 550px;
  overflow: hidden;
  width: 100%;
  border: 1px solid rgba(219, 219, 219, 1);
  border-radius: 10px;
  margin: 0;
  padding: 0;
}

.postItem > header > span {
  color: #030303;
  font-size: 1.25em;
  font-weight: 700;
  margin: 10px 0;
}

.postItem {
  min-height: inherit;
}

.postItem > header {
  display: flex;
  justify-content: center;
  align-items: center;
  border-bottom: 1px solid rgba(219, 219, 219, 1);
  background-color: #fafafa;
}

.postItem > article {
  min-height: inherit;
}

.postItem > section {
  min-height: inherit;
}

#selectImg > article {
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
}

.inputFile {
  width: 0.1px;
  height: 0.1px;
  opacity: 0;
  overflow: hidden;
  position: absolute;
  z-index: -1;
}

.inputFile + label {
  font-size: 1.25em;
  font-weight: 700;
  color: white;
  background-color: rgb(32, 128, 240);
  display: inline-block;
  border-radius: 3px;
  padding: 8px;
}

.inputFile:focus + label,
.inputFile + label:hover {
  background-color: #0971e8;
}

.inputFile + label {
  cursor: pointer; /* 小手光标*/
}

.inputFile:focus + label {
  outline: 1px dotted #8cb6f6;
  outline: -webkit-focus-ring-color auto 5px;
}

.inputFile + label * {
  pointer-events: none;
}

#dropTips {
  margin-bottom: 1rem;
  margin-top: 1rem;
  font-size: 1.5rem;
  /*font-weight: 700;*/
  color: #131313;
}

#editImg > section {
  display: flex;
  justify-content: space-around;
}

#editImgHeader {
  display: flex;
  justify-content: space-between;
}

#editImgHeaderBtn {
  margin-right: 1rem;
  margin-left: 1rem;
}

#editPostHeader {
  display: flex;
  justify-content: space-between;
}

.editPostHeaderBtn {
  margin-right: 1rem;
  margin-left: 1rem;
}

#leftBarImg {
  padding: 0;
  margin: 0;
  display: flex;
  flex-direction: column;
  justify-content: space-around;
  align-items: center;
  min-height: inherit;
  max-width: 10rem;
  width: 8rem;
  border-right: 1px solid rgba(219, 219, 219, 1);
  overflow: hidden;
  flex-grow: 0.3;
}

#leftBarImg > div {
  padding-top: 1rem;
}

#rightEditImg {
  flex-grow: 0.7;
  width: 100%;
}

footer {
  display: flex;
  justify-content: center;
  padding-top: 1rem;
  padding-bottom: 1rem;
}


#editImgSection {
  display: flex;
}

#editPostContent {
  flex-grow: 2;
  width: 100%;
  display: flex;
  flex-direction: column;
  justify-content: flex-start;
  align-items: flex-start;
}

#editPostContent {
  padding: 10px;
}

#editPostContent > div {
  margin-top: 10px;
  /*margin-left: 10px;*/
}

#editPostImg {
  flex-grow: 5;
  display: flex;
  flex-wrap: wrap;
  width: 100%;
  /*background-color: #77B9EF7F;*/
  padding: 1rem;
  border-right: 1px solid rgba(219, 219, 219, 1);
  /*border-radius: 5px;*/
}


.carousel-img {
  width: 100%;
  height: 518px;
  /*  图片剧中*/
}

.inputComment{
  width: 100%;
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
}

</style>
