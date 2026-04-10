<?php

return [

    /*
    |--------------------------------------------------------------------------
    | バリデーション言語行
    |--------------------------------------------------------------------------
    |
    | 以下の言語行はバリデタークラスにより使用されるデフォルトのエラー
    | メッセージです。サイズルールのようにいくつかのバリデーションを
    | 持っているものもあります。メッセージはご自由に調整してください。
    |
    */

    'accepted' => ':attributeを承認してください。',
    'accepted_if' => ':otherが:valueの場合、:attributeを承認してください。',
    'active_url' => ':attributeは有効なURLではありません。',
    'after' => ':attributeには、:date以降の日付を指定してください。',
    'after_or_equal' => ':attributeには、:date以降（同日を含む）の日付を指定してください。',
    'alpha' => ':attributeには、アルファベットのみ使用できます。',
    'alpha_dash' => ':attributeには、英数字・ハイフン・アンダースコアのみ使用できます。',
    'alpha_num' => ':attributeには、英数字のみ使用できます。',
    'any_of' => ':attributeの値が正しくありません。',
    'array' => ':attributeには、配列を指定してください。',
    'ascii' => ':attributeには、半角英数字と記号のみ使用できます。',
    'before' => ':attributeには、:date以前の日付を指定してください。',
    'before_or_equal' => ':attributeには、:date以前（同日を含む）の日付を指定してください。',
    'between' => [
        'array' => ':attributeの項目数は、:min個から:max個の間で指定してください。',
        'file' => ':attributeのファイルサイズは、:min KBから:max KBの間で指定してください。',
        'numeric' => ':attributeは、:minから:maxの間で指定してください。',
        'string' => ':attributeは、:min文字から:max文字の間で指定してください。',
    ],
    'boolean' => ':attributeには、真偽値（true/false）を指定してください。',
    'can' => ':attributeに権限のない値が含まれています。',
    'confirmed' => ':attributeの確認用フィールドが一致しません。',
    'contains' => ':attributeに必要な値が含まれていません。',
    'current_password' => 'パスワードが正しくありません。',
    'date' => ':attributeには、有効な日付を指定してください。',
    'date_equals' => ':attributeには、:dateと同じ日付を指定してください。',
    'date_format' => ':attributeの形式は、:formatと一致している必要があります。',
    'decimal' => ':attributeは、小数点以下が:decimal桁である必要があります。',
    'declined' => ':attributeを拒否してください。',
    'declined_if' => ':otherが:valueの場合、:attributeを拒否してください。',
    'different' => ':attributeと:otherは、異なる必要があります。',
    'digits' => ':attributeは、:digits桁の数字である必要があります。',
    'digits_between' => ':attributeは、:min桁から:max桁の間である必要があります。',
    'dimensions' => ':attributeの画像サイズが正しくありません。',
    'distinct' => ':attributeに重複した値があります。',
    'doesnt_contain' => ':attributeに以下の値を含めることはできません：:values',
    'doesnt_end_with' => ':attributeの末尾を以下のいずれかにすることはできません：:values',
    'doesnt_start_with' => ':attributeの先頭を以下のいずれかにすることはできません：:values',
    'email' => ':attributeには、有効なメールアドレスを指定してください。',
    'encoding' => ':attributeは、:encodingでエンコードされている必要があります。',
    'ends_with' => ':attributeの末尾は、以下のいずれかである必要があります：:values',
    'enum' => '選択された:attributeは正しくありません。',
    'exists' => '選択された:attributeは正しくありません。',
    'extensions' => ':attributeの拡張子は、以下のいずれかである必要があります：:values',
    'file' => ':attributeには、ファイルを指定してください。',
    'filled' => ':attributeには、値を入力してください。',
    'gt' => [
        'array' => ':attributeの項目数は、:value個より多い必要があります。',
        'file' => ':attributeのファイルサイズは、:value KBより大きい必要があります。',
        'numeric' => ':attributeは、:valueより大きい必要があります。',
        'string' => ':attributeは、:value文字より長い必要があります。',
    ],
    'gte' => [
        'array' => ':attributeの項目数は、:value個以上である必要があります。',
        'file' => ':attributeのファイルサイズは、:value KB以上である必要があります。',
        'numeric' => ':attributeは、:value以上である必要があります。',
        'string' => ':attributeは、:value文字以上である必要があります。',
    ],
    'hex_color' => ':attributeには、有効な16進数のカラーコードを指定してください。',
    'image' => ':attributeには、画像ファイルを指定してください。',
    'in' => '選択された:attributeは正しくありません。',
    'in_array' => ':attributeが:otherに存在しません。',
    'in_array_keys' => ':attributeには、以下のキーのいずれかが含まれている必要があります：:values',
    'integer' => ':attributeには、整数を指定してください。',
    'ip' => ':attributeには、有効なIPアドレスを指定してください。',
    'ipv4' => ':attributeには、有効なIPv4アドレスを指定してください。',
    'ipv6' => ':attributeには、有効なIPv6アドレスを指定してください。',
    'json' => ':attributeには、有効なJSON文字列を指定してください。',
    'list' => ':attributeはリスト形式である必要があります。',
    'lowercase' => ':attributeは英小文字である必要があります。',
    'lt' => [
        'array' => ':attributeの項目数は、:value個より少ない必要があります。',
        'file' => ':attributeのファイルサイズは、:value KBより小さい必要があります。',
        'numeric' => ':attributeは、:valueより小さい必要があります。',
        'string' => ':attributeは、:value文字より短い必要があります。',
    ],
    'lte' => [
        'array' => ':attributeの項目数は、:value個以下である必要があります。',
        'file' => ':attributeのファイルサイズは、:value KB以下である必要があります。',
        'numeric' => ':attributeは、:value以下である必要があります。',
        'string' => ':attributeは、:value文字以下である必要があります。',
    ],
    'mac_address' => ':attributeには、有効なMACアドレスを指定してください。',
    'max' => [
        'array' => ':attributeの項目数は、:max個以下である必要があります。',
        'file' => ':attributeのファイルサイズは、:max KB以下である必要があります。',
        'numeric' => ':attributeは、:max以下である必要があります。',
        'string' => ':attributeは、:max文字以下である必要があります。',
    ],
    'max_digits' => ':attributeは、:max桁以下である必要があります。',
    'mimes' => ':attributeのファイルタイプは、以下のいずれかである必要があります：:values',
    'mimetypes' => ':attributeのファイルタイプは、以下のいずれかである必要があります：:values',
    'min' => [
        'array' => ':attributeの項目数は、少なくとも:min個必要です。',
        'file' => ':attributeのファイルサイズは、少なくとも:min KB必要です。',
        'numeric' => ':attributeは、:min以上である必要があります。',
        'string' => ':attributeは、少なくとも:min文字必要です。',
    ],
    'min_digits' => ':attributeは、少なくとも:min桁必要です。',
    'missing' => ':attributeは入力しないでください。',
    'missing_if' => ':otherが:valueの場合、:attributeは入力しないでください。',
    'missing_unless' => ':otherが:valueでない限り、:attributeは入力しないでください。',
    'missing_with' => ':valuesが存在する場合、:attributeは入力しないでください。',
    'missing_with_all' => ':valuesがすべて存在する場合、:attributeは入力しないでください。',
    'multiple_of' => ':attributeは、:valueの倍数である必要があります。',
    'not_in' => '選択された:attributeは正しくありません。',
    'not_regex' => ':attributeの形式が正しくありません。',
    'numeric' => ':attributeには、数字を指定してください。',
    'password' => [
        'letters' => ':attributeには、少なくとも1文字のアルファベットを含める必要があります。',
        'mixed' => ':attributeには、少なくとも1文字ずつの英大文字と英小文字を含める必要があります。',
        'numbers' => ':attributeには、少なくとも1文字の数字を含める必要があります。',
        'symbols' => ':attributeには、少なくとも1文字の記号を含める必要があります。',
        'uncompromised' => '入力された:attributeは、情報漏洩により危険にさらされている可能性があります。別の:attributeを選択してください。',
    ],
    'present' => ':attributeが存在している必要があります。',
    'present_if' => ':otherが:valueの場合、:attributeが存在している必要があります。',
    'present_unless' => ':otherが:valueでない限り、:attributeが存在している必要があります。',
    'present_with' => ':valuesが存在する場合、:attributeが存在している必要があります。',
    'present_with_all' => ':valuesがすべて存在する場合、:attributeが存在している必要があります。',
    'prohibited' => ':attributeの入力は禁止されています。',
    'prohibited_if' => ':otherが:valueの場合、:attributeの入力は禁止されています。',
    'prohibited_if_accepted' => ':otherが承認されている場合、:attributeの入力は禁止されています。',
    'prohibited_if_declined' => ':otherが拒否されている場合、:attributeの入力は禁止されています。',
    'prohibited_unless' => ':otherが:valuesに含まれていない限り、:attributeの入力は禁止されています。',
    'prohibits' => ':attributeを入力すると、:otherを入力することはできません。',
    'regex' => ':attributeの形式が正しくありません。',
    'required' => ':attributeは必須項目です。',
    'required_array_keys' => ':attributeには、以下のキーが含まれている必要があります：:values',
    'required_if' => ':otherが:valueの場合、:attributeは必須です。',
    'required_if_accepted' => ':otherが承認されている場合、:attributeは必須です。',
    'required_if_declined' => ':otherが拒否されている場合、:attributeは必須です。',
    'required_unless' => ':otherが:valuesに含まれていない限り、:attributeは必須です。',
    'required_with' => ':valuesが存在する場合、:attributeは必須です。',
    'required_with_all' => ':valuesがすべて存在する場合、:attributeは必須です。',
    'required_without' => ':valuesが存在しない場合、:attributeは必須です。',
    'required_without_all' => ':valuesがすべて存在しない場合、:attributeは必須です。',
    'same' => ':attributeと:otherが一致しません。',
    'size' => [
        'array' => ':attributeの項目数は、:size個である必要があります。',
        'file' => ':attributeのファイルサイズは、:size KBである必要があります。',
        'numeric' => ':attributeは、:sizeである必要があります。',
        'string' => ':attributeは、:size文字である必要があります。',
    ],
    'starts_with' => ':attributeの先頭は、以下のいずれかである必要があります：:values',
    'string' => ':attributeには、文字列を指定してください。',
    'timezone' => ':attributeには、有効なタイムゾーンを指定してください。',
    'unique' => 'この:attributeは既に登録されています。',
    'uploaded' => ':attributeのアップロードに失敗しました。',
    'uppercase' => ':attributeは英大文字である必要があります。',
    'url' => ':attributeには、有効なURLを指定してください。',
    'ulid' => ':attributeには、有効なULIDを指定してください。',
    'uuid' => ':attributeには、有効なUUIDを指定してください。',

    /*
    |--------------------------------------------------------------------------
    | Custom バリデーション言語行
    |--------------------------------------------------------------------------
    |
    | "属性.ルール"の規約でキーを指定することでカスタムバリデーション
    | メッセージを定義できます。指定した属性ルールに対する特定の
    | カスタム言語行を手早く指定できます。
    |
    */

    'custom' => [
        'attribute-name' => [
            'rule-name' => 'custom-message',
        ],
    ],

    /*
    |--------------------------------------------------------------------------
    | カスタムバリデーション属性名
    |--------------------------------------------------------------------------
    |
    | 以下の言語行は、例えば"email"の代わりに「メールアドレス」のように、
    | 読み手にフレンドリーな表現でプレースホルダーを置き換えるために指定する
    | 言語行です。これはメッセージをよりきれいに表示するために役に立ちます。
    |
    */

    'attributes' => [],

];
