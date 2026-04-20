module UseCase
  module Gate
    IssueDto  = Struct.new(:access_token, :member_id, keyword_init: true)
    VerifyDto = Struct.new(:identifier, :token, keyword_init: true)
  end
end
