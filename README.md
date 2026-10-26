# KOSAKU - 香りづくり体験予約管理システム

## プロジェクト概要

KOSAKUは、Web制作総合演習1で企画・設計した「旅香 - TABIKA -」をもとに、Web制作総合演習2の卒業制作として開発した香りづくり体験の予約管理システムです。

利用者向けにはワークショップの予約と問い合わせ、スタッフ向けには予約・問い合わせの確認や対応状況の管理機能を提供します。現時点では、旅香の企画をもとにした独立した予約管理アプリケーションであり、旅香との実際のシステム連携は実装していません。

## 公開URL

- Render公開URL: [https://kousaku.onrender.com/](https://kousaku.onrender.com/)
- GitHubリポジトリ: [https://github.com/tonchiw3-tech/kousaku](https://github.com/tonchiw3-tech/kousaku)

## 主な機能

### 利用者向け

- ワークショップ予約
- 予約番号の発行
- 参加人数に応じた料金計算
- 問い合わせ登録
- 問い合わせ番号による返信状況の確認

### スタッフ向け

- スタッフログイン
- 予約ステータス管理（受付・確認・準備完了・体験完了・キャンセル）
- 予約のキャンセル・削除
- 完了済み予約の訂正と訂正履歴管理
- 月間カレンダーによる予約・準備状況の確認
- 問い合わせの対応状況・返信内容の管理

## 使用技術

| 分類 | 技術 |
| --- | --- |
| 言語 | Java 21 |
| フレームワーク | Spring Boot 4.0.8 |
| データアクセス | MyBatis 4.0.1 |
| データベース（ローカル） | MySQL 8.0 |
| データベース（Render提出用デモ） | H2 Database |
| 画面テンプレート | Thymeleaf |
| フロントエンド | HTML / CSS / JavaScript |
| 認証・認可 | Spring Security |
| テスト | JUnit 5 / Spring Boot Test / MockMvc |
| コンテナ | Docker / Docker Compose |
| バージョン管理 | Git / GitHub |
| 公開環境 | Render Free |

## システム構成

```text
利用者・スタッフ
       ↓
   Webブラウザ
       ↓
Spring Boot Controller
       ↓
      Service
       ↓
 MyBatis Mapper
       ↓
    Database
```

ローカル環境ではMySQL、Render環境では`render`プロファイルのインメモリH2を使用します。

## データベース構成

| テーブル名 | 用途 |
| --- | --- |
| `reservations` | 予約情報、料金、予約ステータス |
| `reservation_status_corrections` | 予約ステータスの訂正履歴 |
| `inquiries` | 問い合わせ、対応状況、返信内容 |

ローカル環境の定義は [schema.sql](src/main/resources/schema.sql)、Render用H2の定義は [schema-render.sql](src/main/resources/schema-render.sql) を参照してください。

## 主なAPI

| 機能 | メソッド | エンドポイント | 権限 |
| --- | --- | --- | --- |
| 予約登録 | `POST` | `/api/reservations` | 公開 |
| 問い合わせ登録 | `POST` | `/api/inquiries` | 公開 |
| 問い合わせ状況確認 | `GET` | `/api/inquiries/status?inquiryNumber={番号}` | 公開 |
| 予約一覧・詳細 | `GET` | `/api/staff/reservations` / `/api/staff/reservations/{id}` | スタッフ |
| 予約ステータス変更 | `PATCH` | `/api/staff/reservations/{id}/status` | スタッフ |
| 予約キャンセル | `PATCH` | `/api/staff/reservations/{id}/cancel` | スタッフ |
| 予約削除 | `DELETE` | `/api/staff/reservations/{id}` | スタッフ |
| 予約訂正 | `PATCH` | `/api/staff/reservations/{id}/correction` | スタッフ |
| 月間カレンダー | `GET` | `/api/staff/calendar?year={年}&month={月}` | スタッフ |
| 問い合わせ一覧・詳細 | `GET` | `/api/staff/inquiries` / `/api/staff/inquiries/{id}` | スタッフ |
| 問い合わせ対応状況・返信 | `PATCH` | `/api/staff/inquiries/{id}/status` / `/reply` | スタッフ |

スタッフ用APIと画面はSpring Securityで保護しています。ログイン用の実際のID・パスワードはセキュリティ上READMEには掲載しません。

## ローカル環境での起動方法

### 前提条件

- Java 21
- Docker Desktop
- Git

```bash
git clone https://github.com/tonchiw3-tech/kousaku.git
cd kousaku
```

`.env.example`を参考に`.env`を作成し、データベース接続情報とスタッフ認証情報を設定してください。`.env`はGitへコミットしないでください。

```bash
docker compose up -d
```

Windowsでは次のコマンドで起動します。

```powershell
.\mvnw.cmd spring-boot:run
```

macOS / Linuxでは次のコマンドを使用します。

```bash
./mvnw spring-boot:run
```

- 利用者向け画面: <http://localhost:8080>
- スタッフ用予約管理画面: <http://localhost:8080/staff/reservations>

## Renderへのデプロイ

Dockerfileを使用するWeb ServiceとしてRenderにデプロイします。`SPRING_PROFILES_ACTIVE=render`を設定すると、Render用H2プロファイルが有効になります。スタッフ認証情報はRenderの環境変数に設定し、READMEやGitHubには記載しません。

Renderへのデプロイは成功しており、ステータスはLiveです。確認対象のコミットは`7b48fba`、公開URLは[https://kousaku.onrender.com/](https://kousaku.onrender.com/)です。

Render Freeにはスリープがあり、インメモリH2のデータは再起動時に初期化されます。そのため、公開環境は提出用デモであり、永続運用を目的とした本番環境ではありません。

## テスト結果

### Maven / JUnit

最新のSurefire実行記録では、JUnitテスト29件が全件成功しました。

- 成功: 29件
- 失敗: 0件
- エラー: 0件
- スキップ: 0件
- `ReservationServiceTest`: 6件成功
- Mavenパッケージ作成: 成功（`BUILD SUCCESS`）

### Render用H2プロファイル・HTTP確認

- H2プロファイルでの起動成功
- トップ画面: HTTP 200
- 予約登録: HTTP 201
- 問い合わせ登録: HTTP 201
- 未認証でのスタッフ画面アクセス: HTTP 302

## 実装上の注意

- Render環境は提出用デモのため、H2のデータは再起動時に初期化されます。
- Render Freeのスリープ後は、初回アクセスに時間がかかる場合があります。
- 祝日判定は未実装です。予約可能日の判定は土曜日・日曜日を対象としています。
- `.env`、データベースのパスワード、スタッフログイン情報は公開しません。
- 旅香との実際のデータ連携・システム連携は未実装です。

## 関連プロジェクト・設計資料

本システムは、Web制作総合演習1で企画した「旅香 - TABIKA -」の企画・設計をもとにしています。KOSAKU内の設計資料は次のとおりです。

- [仕様書](documents/docs/07-specification.html)
- [DB設計書](documents/docs/08-db-design.html)
- [テスト報告書](documents/docs/09-test-report.html)
- Render設定メモ: [RENDER_DEPLOY.md](RENDER_DEPLOY.md)

## 制作について

本アプリケーションは、職業訓練のWeb制作総合演習2における卒業制作です。Web制作総合演習1で企画した体験サービスを題材に、Java / Spring BootによるWebアプリケーション、MyBatisによるデータアクセス、Spring Securityによる認証・認可、MySQL・H2を使った環境分離、JUnitによるテスト、Dockerによるデプロイ構成を学習・実装しました。

本プロジェクトは学習・卒業制作を目的としたアプリケーションであり、実際の店舗予約サービスとしての運用を保証するものではありません。
