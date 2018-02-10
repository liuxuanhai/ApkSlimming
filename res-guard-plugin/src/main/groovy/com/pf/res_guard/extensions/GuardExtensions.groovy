package com.pf.res_guard.extensions

class GuardExtensions {

    /**
     * 输出路径
     */
    def outputPath
    /**
     * 7z 的集合
     */
    def sevenZDirs

    def sevenZDir(String sevenZDir) {
        if (!sevenZDirs.contains(sevenZDir)) {
            sevenZDirs << sevenZDir
        }
    }

    def sevenZDirs(String... sevenZDirs) {
        sevenZDirs.each {
            sevenZDir(it)
        }
    }


    @Override
    public String toString() {
        return "GuardExtensions{" +
                "outputPath=" + outputPath +
                ", sevenZDirs=" + sevenZDirs +
                '}';
    }
}