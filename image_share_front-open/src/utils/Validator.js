const Validator = {
    isEmail(email) {
        let emailReg = /^[0-9a-zA-Z_.-]+[@][0-9a-zA-Z_.-]+([.][a-zA-Z]+){1,2}$/;
        return emailReg.test(email)
    },
    isPassword(pwd) {
        let pwdReg = /^([a-zA-Z0-9#$%&*!@.,_]){6,20}$/;
        return pwdReg.test(pwd)
    },
    isSnakeName(snakeName) {
        let snakeNameReg = /^[a-zA-Z0-9_\u4e00-\u9fa5]+$/
        return snakeNameReg.test(snakeName)
    },
    isImageCaptcha(captcha) {
        if (!(typeof (captcha) === 'string')) {
            return false
        }
        return captcha.length === 4;
    }, isEmailCaptcha(captcha) {
        if (!(typeof (captcha) === 'string')) {
            return false
        }
        return captcha.length === 6;
    }

}

export default Validator