;; Copyright (c) 2021-2022 Vitaly Samigullin and contributors. All rights reserved.
;;
;; This program and the accompanying materials are made available under the
;; terms of the Eclipse Public License 2.0 which is available at
;; http://www.eclipse.org/legal/epl-2.0.
;;
;; This Source Code may also be made available under the following Secondary
;; Licenses when the conditions for such availability set forth in the Eclipse
;; Public License, v. 2.0 are satisfied: GNU General Public License as published by
;; the Free Software Foundation, either version 2 of the License, or (at your
;; option) any later version, with the GNU Classpath Exception which is available
;; at https://www.gnu.org/software/classpath/license.html.
;;
;; SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0

(ns dienstplan.spec
  (:gen-class)
  (:require
   [clojure.spec.alpha :as s]))

;;;;;;;;;;;;;;;;;;;;;
;; Conform helpers ;;
;;;;;;;;;;;;;;;;;;;;;

(defmacro with-conformer
  [[bind] & body]
  `(s/conformer
    (fn [~bind]
      (try
        ~@body
        (catch Exception e#
          ::s/invalid)))))

(def ->bool
  (with-conformer [val]
    (boolean (Boolean/valueOf val))))

(s/def ::->bool ->bool)

(def ->int
  (with-conformer [val]
    (Integer/parseInt val)))

(s/def ::->int ->int)
;;(s/conform ::->int "123")

(def ->uuid
  (with-conformer [val]
    (java.util.UUID/fromString val)))

(s/def ::->uuid
  (s/and
   string?
   not-empty
   ->uuid))
;;(s/conform ::->uuid "a2ab7fdb-b3f7-4aa6-bde5-7c37a0a39d71")

;;;;;;;;;;;;;;;;;;
;; Common specs ;;
;;;;;;;;;;;;;;;;;;

;; Basics

(s/def ::kw keyword?)
(s/def ::str string?)
(s/def ::nillable-str (s/nilable ::str))
(s/def ::non-empty-str (s/and ::str not-empty))
(s/def ::boolean-str (s/and #{"true" "false"} ::->bool))

;; Domain-related
(def timeout-max-ms (* 65535 1000))
(def pool-size-max 300)

(s/def ::ephemeral-port (s/and ::->int (s/int-in 1024 (inc 65535))))
(s/def ::pool-size (s/and ::->int (s/int-in 1 (inc pool-size-max))))
(s/def ::timeout (s/and ::->int (s/int-in 1000 (inc timeout-max-ms))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Application Configuration ;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(s/def ::application-config
  (s/keys :req-un [::application ::server ::slack ::alerts ::db]))

;; Application

(s/def :application/name ::non-empty-str)
(s/def :application/version ::non-empty-str)
(s/def :application/env ::non-empty-str)
(s/def :application/debug ::boolean-str)

(s/def ::application
  (s/keys
   :req-un
   [:application/name
    :application/version
    :application/env
    :application/debug]))

;; Server

(s/def :server/port ::ephemeral-port)
(s/def :server/loglevel #{"DEBUG" "INFO" "WARN" "ERROR" "FATAL"})
(s/def :server/access-log ::boolean-str)

(s/def ::server
  (s/keys
   :req-un
   [:server/port
    :server/loglevel
    :server/access-log]))

;; Slack

(s/def :slack/token ::non-empty-str)
(s/def :slack/sign ::non-empty-str)

(s/def ::slack
  (s/keys
   :req-un
   [:slack/token
    :slack/sign]))

;; Alerts

(s/def :alerts/sentry ::str)

(s/def ::alerts
  (s/keys
   :req-un
   [:alerts/sentry]))

;; DB

(s/def :db/adapter #{"postgresql"})
(s/def :db/server-name ::non-empty-str)
(s/def :db/port-number ::ephemeral-port)
(s/def :db/database-name ::non-empty-str)
(s/def :db/username ::non-empty-str)
(s/def :db/password ::non-empty-str)
(s/def :db/minimum-idle ::pool-size)
(s/def :db/maximum-pool-size ::pool-size)
(s/def :db/connection-timeout ::timeout)

(s/def ::db
  (s/keys
   :req-un
   [:db/adapter
    :db/server-name
    :db/port-number
    :db/database-name
    :db/username
    :db/password
    :db/minimum-idle
    :db/maximum-pool-size
    :db/connection-timeout]))

;;;;;;;;;;;;;;;;;;
;; Bot Commands ;;
;;;;;;;;;;;;;;;;;;

(s/def :bot-cmd-context/channel ::str)
(s/def :bot-cmd-context/ts ::str)
(s/def :bot-cmd-context/team ::str)

(s/def :bot-cmd-common/context
  (s/keys
   :req-un
   [:bot-cmd-context/channel
    :bot-cmd-context/ts
    :bot-cmd-context/team]))

(s/def :bot-cmd-common/command ::kw)

(s/def :bot-cmd-args/name ::nillable-str)
(s/def :bot-cmd-args/description ::nillable-str)
(s/def :bot-cmd-args/users (s/nilable (s/+ string?)))

(s/def :bot-cmd-default/args
  (s/keys
   :req-un
   [:bot-cmd-args/name]))

(s/def ::bot-cmd-default
  (s/keys
   :req-un
   [:bot-cmd-common/context
    :bot-cmd-common/command
    :bot-cmd-default/args]))

(s/def :bot-cmd-create/args
  (s/keys
   :req-un
   [:bot-cmd-args/name
    :bot-cmd-args/users
    :bot-cmd-args/description]))

(s/def ::bot-cmd-create
  (s/keys
   :req-un
   [:bot-cmd-common/context
    :bot-cmd-common/command
    :bot-cmd-create/args]))

(s/def :bot-cmd-help/args
  (s/keys
   :req-un
   [:bot-cmd-args/description]))

(s/def ::bot-cmd-help
  (s/keys
   :req-un
   [:bot-cmd-common/context
    :bot-cmd-common/command]))

(s/def ::bot-cmd-list
  (s/keys
   :req-un
   [:bot-cmd-common/context
    :bot-cmd-common/command]))

;;;;;;;;;;;;;;;;;;
;; Bot Response ;;
;;;;;;;;;;;;;;;;;;

(s/def :bot-response/channel ::str)
(s/def :bot-response/text ::str)

(s/def ::bot-response
  (s/keys
   :req-un
   [:bot-response/channel
    :bot-response/text]))
