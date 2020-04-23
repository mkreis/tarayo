(ns tarayo.mail.mime.multipart.body-test
  (:require
   [clojure.java.io :as io]
   [clojure.test :as t]
   [tarayo.mail.mime.multipart.body :as sut])
  (:import
   java.nio.file.Files
   (javax.mail.internet
    MimeBodyPart
    MimeUtility)))

(t/deftest make-bodypart-text-html-test
  (t/testing "string type"
    (let [part {:content-type "text/html" :content "<h1>hello</h1>"}
          bp (sut/make-bodypart part "UTF-8")]
      (t/is (instance? MimeBodyPart bp))

      (t/is (= "<h1>hello</h1>" ^String (.getContent bp)))
      (t/is (= "text/html; charset=UTF-8" (.getContentType bp)))))

  (t/testing "keyword type"
    (let [part {:content-type :text/html :content "<h1>hello</h1>"}
          bp (sut/make-bodypart part "UTF-8")]
      (t/is (instance? MimeBodyPart bp))
      (t/is (= "<h1>hello</h1>" ^String (.getContent bp)))
      (t/is (= "text/html; charset=UTF-8" (.getContentType bp))))))

(t/deftest make-bodypart-test
  (t/testing "inline"
    (let [part {:content (io/file "project.clj") :id (str (gensym))}
          bp (sut/make-bodypart part "UTF-8")]
      (t/is (instance? MimeBodyPart bp))
      (t/is (= ["text/x-clojure"] (seq (.getHeader bp "Content-Type"))))
      (t/is (= "project.clj" (.getFileName bp)))
      (t/is (= "inline" (.getDisposition bp)))))

  (t/testing "specify content-type"
    (let [part {:content (io/file "project.clj")
                :content-type "text/plain"}
          bp (sut/make-bodypart part "UTF-8")]
      (t/is (instance? MimeBodyPart bp))
      (t/is (= ["text/plain"]
               (seq (.getHeader bp "Content-Type"))))))

  (t/testing "attachment"
    (let [part {:content (io/file "project.clj")}
          bp (sut/make-bodypart part "UTF-8")]
      (t/is (instance? MimeBodyPart bp))
      (t/is (= ["text/x-clojure"] (seq (.getHeader bp "Content-Type"))))
      (t/is (= "project.clj" (.getFileName bp)))
      (t/is (= "attachment" (.getDisposition bp)))))

  (t/testing "attachment by path string"
    (let [part {:content (io/file "project.clj")}
          bp (sut/make-bodypart part "UTF-8")]
      (t/is (instance? MimeBodyPart bp))
      (t/is (= ["text/x-clojure"] (seq (.getHeader bp "Content-Type"))))
      (t/is (= "project.clj" (.getFileName bp)))
      (t/is (= "attachment" (.getDisposition bp)))))

  (t/testing "overwriting attachment filename"
    (let [part {:content (io/file "project.clj")
                :filename "overwrite.clj"}
          bp (sut/make-bodypart part "UTF-8")]
      (t/is (instance? MimeBodyPart bp))
      (t/is (= ["text/x-clojure"] (seq (.getHeader bp "Content-Type"))))
      (t/is (= "overwrite.clj" (.getFileName bp)))
      (t/is (= "attachment" (.getDisposition bp))))))

(t/deftest make-bodypart-with-content-id-test
  (let [part {:content (io/file "project.clj") :id "foo-id"}
        bp (sut/make-bodypart part "UTF-8")]
    (t/is (instance? MimeBodyPart bp))
    (t/is (= "<foo-id>" (.getContentID bp)))))

(t/deftest make-bodypart-with-utf-8-file-name-test
  (let [part {:content (io/resource "多羅葉.txt")}
        bp (sut/make-bodypart part "UTF-8")]
    (t/is (instance? MimeBodyPart bp))
    (t/is (= ["text/plain"] (seq (.getHeader bp "Content-Type"))))
    (t/is (= (MimeUtility/encodeText "多羅葉.txt" "UTF-8" nil)
             (.getFileName bp)))))

(t/deftest make-bodypart-with-content-encoding-test
  (let [part {:content (io/file "project.clj")
              :content-encoding "base64"}
        bp (sut/make-bodypart part "UTF-8")]
    (t/is (instance? MimeBodyPart bp))
    (t/is (= ["base64"] (seq (.getHeader bp "Content-Transfer-Encoding"))))))

(t/deftest make-bodypart-with-bytes-attachment-test
  (t/testing "positive"
    (let [file (io/file "project.clj")
          part {:content (Files/readAllBytes (.toPath file))
                :content-type "text/x-clojure"
                :filename "foo.clj"}
          bp (sut/make-bodypart part "UTF-8")]
      (t/is (instance? MimeBodyPart bp))
      (t/is (= ["text/x-clojure"] (seq (.getHeader bp "Content-Type"))))
      (t/is (= "foo.clj" (.getFileName bp)))
      (t/is (= "attachment" (.getDisposition bp)))))

  (t/testing "no content-type"
    (t/is (thrown? AssertionError
            (sut/make-bodypart {:content (.getBytes "foo")
                                :filename "foo.clj"}
                               "UTF-8"))))
  (t/testing "empty content-type"
    (t/is (thrown? AssertionError
            (sut/make-bodypart {:content (.getBytes "foo")
                                :content-type ""
                                :filename "foo.clj"}
                               "UTF-8"))))
  (t/testing "no filename"
    (t/is (thrown? AssertionError
            (sut/make-bodypart {:content (.getBytes "foo")
                                :content-type "text/plain"}
                               "UTF-8"))))
  (t/testing "empty filename"
    (t/is (thrown? AssertionError
            (sut/make-bodypart {:content (.getBytes "foo")
                                :content-type "text/plain"
                                :filename ""}
                               "UTF-8")))))
