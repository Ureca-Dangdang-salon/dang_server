package com.dangdangsalon.chatgpt;

public enum DogCelebrityMapping {

    GOLDEN_RETRIEVER("골든 리트리버", "박해진", "https://img.hankyung.com/photo/201601/03.9412505.1.jpg"),
    CHIHUAHUA("치와와", "김국진", "https://cdn.hankyung.com/photo/202405/03.36635111.1.jpg"),
    JINDO("진돗개", "서인국", "https://entertainimg.kbsmedia.co.kr/cms/uploads/CONTENTS_20220922155600_54249760516b76bf6d3f816cc872281f.jpg"),
    CHOW_CHOW("차우차우", "고창석", "https://image.news1.kr/system/photos/2016/5/31/1956615/article.jpg"),
    BULLDOG("불독", "김구라", "https://img.hankyung.com/photo/202004/AKR20200402161700005_01_i.jpg"),
    BULL_TERRIER("불테리어", "박명수", "https://m.segye.com/content/image/2024/04/03/20240403506251.png"),
    SHEPHERD("셰퍼드", "이연복", "https://img6.yna.co.kr/etc/inner/KR/2019/10/29/AKR20191029084100005_01_i_P2.jpg"),
    SHIH_TZU("시츄", "채수빈", "https://spnimage.edaily.co.kr/images/Photo/files/NP/S/2019/01/PS19011100033.jpg"),
    POODLE("푸들", "윈터", "https://search.pstatic.net/common/?src=http%3A%2F%2Fimgnews.naver.net%2Fimage%2F609%2F2023%2F12%2F15%2F202312151957421510_1_20231215195804576.jpg&type=a340"),
    HUSKY("허스키", "이상윤", "https://newsimg.sedaily.com/2019/12/29/1VS9GSK58M_1.jpg"),
    CORGI("웰시코기", "박보영", "https://i.namu.wiki/i/JkML_XrsJw3fSPzxItjfvxNWtF8XkGA8qs3pm7i-n0c7VRjZlC32xt1Vblx2MmOX3io8syX6aoKkaBAUUBLosA.webp"),
    DALMATIAN("달마시안", "한지민", "https://pimg.mk.co.kr/news/cms/202411/08/news-p.v1.20241108.693ff74fdbcc4010aada161b6ef59915_P1.jpg"),
    MALTESE("말티즈", "수지", "https://image.kmib.co.kr/online_image/2022/0629/2022062916360153343_1656488164_0017227708.jpg"),
    PUG("퍼그", "정형돈", "https://cdn.autotribune.co.kr/news/photo/202405/17638_79108_2333.png"),
    POMERANIAN("포메라니안", "태연", "https://www.madtimes.org/news/photo/202302/16670_38219_1911.jpg");

    private final String dogType;
    private final String celebrity;
    private final String imageUrl;

    DogCelebrityMapping(String dogType, String celebrity, String imageUrl) {
        this.dogType = dogType;
        this.celebrity = celebrity;
        this.imageUrl = imageUrl;
    }

    public String getDogType() {
        return dogType;
    }

    public String getCelebrity() {
        return celebrity;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public static DogCelebrityInfo matchCelebrity(String analysisResult) {
        for (DogCelebrityMapping mapping : values()) {
            if (analysisResult.contains(mapping.getDogType())) {
                return new DogCelebrityInfo(mapping.getCelebrity(), mapping.getImageUrl());
            }
        }
        return new DogCelebrityInfo("매칭된 연예인이 없습니다.", null);
    }

    public static class DogCelebrityInfo {
        private final String celebrity;
        private final String imageUrl;

        public DogCelebrityInfo(String celebrity, String imageUrl) {
            this.celebrity = celebrity;
            this.imageUrl = imageUrl;
        }

        public String getCelebrity() {
            return celebrity;
        }

        public String getImageUrl() {
            return imageUrl;
        }
    }
}
