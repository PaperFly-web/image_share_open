export const RandomUtil = {
    uuid,
    randomNum,
    getOneImg,
    getOneImgByIndex
}
export function uuid(len, radix) {

    var chars = '0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz'.split('');

    var uuid = [], i;

    radix = radix || chars.length;



    if (len) {

        // Compact form

        for (i = 0; i < len; i++) uuid[i] = chars[0 | Math.random()*radix];

    } else {

        // rfc4122, version 4 form

        var r;



        // rfc4122 requires these characters

        uuid[8] = uuid[13] = uuid[18] = uuid[23] = '-';

        uuid[14] = '4';



        // Fill in random data.  At i==19 set the high bits of clock sequence as

        // per rfc4122, sec. 4.1.5

        for (i = 0; i < 36; i++) {

            if (!uuid[i]) {

                r = 0 | Math.random()*16;

                uuid[i] = chars[(i == 19) ? (r & 0x3) | 0x8 : r];

            }

        }

    }



    return uuid.join('');

}
//生成从minNum到maxNum的随机数
export function randomNum(minNum,maxNum){
    switch(arguments.length){
        case 1:
            return parseInt(Math.random()*minNum+1,10);
            break;
        case 2:
            return parseInt(Math.random()*(maxNum-minNum+1)+minNum,10);
            break;
        default:
            return 0;
            break;
    }
}
const imageUrls = [
    "https://images.pexels.com/photos/9982086/pexels-photo-9982086.jpeg?cs=srgb&dl=pexels-anastasia-latunova-9982086.jpg&fm=jpg",
    "https://images.pexels.com/photos/7846313/pexels-photo-7846313.jpeg?cs=srgb&dl=pexels-marstion-7846313.jpg&fm=jpg",
    "https://images.pexels.com/photos/8742896/pexels-photo-8742896.jpeg?cs=srgb&dl=pexels-mauricio-borja-8742896.jpg&fm=jpg",
    "https://images.pexels.com/photos/11022402/pexels-photo-11022402.jpeg?cs=srgb&dl=pexels-brett-sayles-11022402.jpg&fm=jpg",
    "https://images.pexels.com/photos/10828146/pexels-photo-10828146.jpeg?cs=srgb&dl=pexels-nikita-igonkin-10828146.jpg&fm=jpg",
    "https://images.pexels.com/photos/10012581/pexels-photo-10012581.jpeg?cs=srgb&dl=pexels-yaroslava-borz-10012581.jpg&fm=jpg",
    "https://images.pexels.com/photos/11255216/pexels-photo-11255216.jpeg?cs=srgb&dl=pexels-arionar-creative-11255216.jpg&fm=jpg",
    "https://images.pexels.com/photos/10212470/pexels-photo-10212470.jpeg?cs=srgb&dl=pexels-u%C4%9Furcan-%C3%B6zmen-10212470.jpg&fm=jpg",
    "https://images.pexels.com/photos/9725715/pexels-photo-9725715.jpeg?cs=srgb&dl=pexels-%D0%B4%D0%B8%D0%B0%D0%BD%D0%B0-%D0%B4%D1%83%D0%BD%D0%B0%D0%B5%D0%B2%D0%B0-9725715.jpg&fm=jpg",
    "https://images.pexels.com/photos/9567499/pexels-photo-9567499.jpeg?cs=srgb&dl=pexels-haili-vandereems-9567499.jpg&fm=jpg",
    "https://images.pexels.com/photos/10058297/pexels-photo-10058297.jpeg?cs=srgb&dl=pexels-evie-shaffer-10058297.jpg&fm=jpg",
    "https://images.pexels.com/photos/11057172/pexels-photo-11057172.jpeg?cs=srgb&dl=pexels-svetlana-romashenko-11057172.jpg&fm=jpg",
    "https://images.pexels.com/photos/11101677/pexels-photo-11101677.jpeg?cs=srgb&dl=pexels-annie-spratt-11101677.jpg&fm=jpg",
    "https://images.pexels.com/photos/11160227/pexels-photo-11160227.jpeg?cs=srgb&dl=pexels-yulia-polyakova-11160227.jpg&fm=jpg",
    "https://images.pexels.com/photos/8602698/pexels-photo-8602698.jpeg?cs=srgb&dl=pexels-%D0%B8%D0%BD%D0%BE-%D0%BD%D0%B5%D0%B8%D0%BD%D0%BE-8602698.jpg&fm=jpg",
    "https://images.pexels.com/photos/4541924/pexels-photo-4541924.jpeg?cs=srgb&dl=pexels-olya-kobruseva-4541924.jpg&fm=jpg",
    "https://images.pexels.com/photos/2171129/pexels-photo-2171129.jpeg?cs=srgb&dl=pexels-lisa-fotios-2171129.jpg&fm=jpg",
    "https://images.pexels.com/photos/3843292/pexels-photo-3843292.jpeg?cs=srgb&dl=pexels-rfstudio-3843292.jpg&fm=jpg",
    "https://images.pexels.com/photos/3126574/pexels-photo-3126574.jpeg?cs=srgb&dl=pexels-rafael-barros-3126574.jpg&fm=jpg",
    "https://images.pexels.com/photos/7047135/pexels-photo-7047135.jpeg?cs=srgb&dl=pexels-aleksey-kuprikov-7047135.jpg&fm=jpg",
    "https://images.pexels.com/photos/10122032/pexels-photo-10122032.jpeg?cs=srgb&dl=pexels-svetlana%F0%9F%8E%9E-10122032.jpg&fm=jpg",
    "https://images.pexels.com/photos/10339223/pexels-photo-10339223.jpeg?cs=srgb&dl=pexels-ekaterina-10339223.jpg&fm=jpg",
    "https://images.pexels.com/photos/10140327/pexels-photo-10140327.jpeg?cs=srgb&dl=pexels-alesia-shapran-10140327.jpg&fm=jpg",
    "https://images.pexels.com/photos/8709703/pexels-photo-8709703.jpeg?cs=srgb&dl=pexels-amina-8709703.jpg&fm=jpg",
    "https://images.pexels.com/photos/10366313/pexels-photo-10366313.jpeg?cs=srgb&dl=pexels-john-de-leon-10366313.jpg&fm=jpg"
]
export function getOneImg(){
    let index = RandomUtil.randomNum(0,imageUrls.length-1);
    return imageUrls[index]
}

export function getOneImgByIndex(index){

    return imageUrls[index]
}