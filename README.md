# MCSMCommand
[MCSManager](https://mcsmanager.com/)のインスタンスを起動/停止するコマンドを追加します

## 前提
- Bukkit
- [MCSManager API](https://docs.mcsmanager.com/apis/get_apikey.html)
- [KyoriAdventureLib](https://github.com/Necnion8/KyoriAdventureLib) (Paper 1.16.5 以降なら不要)

## コマンドと権限
| コマンド         | サブコマンド / 説明                         | 権限                              | デフォルト |
|--------------|-------------------------------------|:--------------------------------|:-----:|
| /mcsmcommand |                                     | mcsmcommand.command.mcsmcommand | OPのみ  |
| 〃            | list<br><sup>インスタンス一覧を表示</sup>      | 〃                               |   〃   |
| 〃            | start (name)<br><sup>起動します</sup>    | 〃                               |   〃   |
| 〃            | stop (name)<br><sup>停止します</sup>     | 〃                               |   〃   |
| 〃            | restart (name)<br><sup>再起動します</sup> | 〃                               |   〃   |
| 〃            | kill (name)<br><sup>強制終了します</sup>   | 〃                               |   〃   |

## 設定
[./plugins/MCSMCommand/config.yml](src/main/resources/bukkit-config.yml)<br>
※ 現在、操作できるノードは設定ファイルによって固定されます
