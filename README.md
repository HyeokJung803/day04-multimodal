# day04-multimodal

Spring AI 멀티모달 기능을 사용해서 오디오 파일을 업로드하고, AI가 음악 장르를 분석하는 예제 프로젝트입니다.

## 구현한 기능

| 기능 | 설명 |
| --- | --- |
| 오디오 업로드 | `MP3`, `WAV` 파일 업로드 지원 |
| 장르 분석 | Gemini 멀티모달 모델에 오디오와 프롬프트를 함께 전달 |
| 구조화 응답 | `MusicGenreResult` record로 대표 장르, 세부 장르, 후보 장르, 신뢰도, 설명 반환 |
| 후보 장르 표시 | 장르 하나만 찍지 않고 가능성 있는 세부 장르 3~5개 표시 |
| 웹 UI | 업로드 영역, 분석 결과 카드, 후보 장르 칩, 신뢰도 바를 가진 화면 구성 |

## 주요 파일

| 파일 | 역할 |
| --- | --- |
| `FindGenreController.java` | 업로드 화면과 분석 요청 처리 |
| `FindGenreService.java` | 파일 검증, 오디오 리소스 변환, AI 프롬프트 호출 |
| `MusicGenreResult.java` | AI 응답을 담는 결과 DTO |
| `templates/genre/upload.html` | 오디오 업로드 및 분석 결과 화면 |
| `templates/genre/result.html` | 별도 결과 화면용 템플릿 |

## 고친 문제

| 문제 | 원인 | 해결 |
| --- | --- | --- |
| 프롬프트 한글 깨짐 | 파일 인코딩/문자열 깨짐으로 모델이 지시를 제대로 읽기 어려웠음 | 프롬프트를 UTF-8 한국어로 다시 작성 |
| 장르를 하나만 반환 | `mainGenre`, `subGenre`, `description`만 있어서 애매한 곡 판단이 약했음 | `candidateGenres`, `confidence` 필드 추가 |
| 힙합 곡이 무조건 트랩으로 나옴 | 모델이 랩, 저역, 강한 비트를 트랩으로 과하게 해석 | 808, 하이햇 롤, 트랩식 스네어 등 핵심 조건이 있을 때만 트랩 선택하도록 수정 |
| 재즈힙합/붐뱁이 팝랩으로 나옴 | 말하듯 부르는 보컬이나 멜로디를 랩/팝랩으로 오해 | 실제 랩 벌스와 팝 후렴이 모두 중심일 때만 `Pop Rap` 선택하도록 수정 |
| `Closer`가 힙합/팝랩으로 분류됨 | 싱토크 보컬을 랩으로 착각 | 팝 보컬 + 신스 리프 + 드롭 구조는 `Electro Pop`, `Dance Pop`, `Future Bass` 우선 |
| 특정 장르만 기준이 자세함 | 힙합 오분류를 잡는 과정에서 힙합 규칙만 비대해짐 | Pop, R&B, Rock, Ballad, Jazz, EDM, Metal, Punk, Indie 등 전체 장르 기준 재정리 |
| 화면이 단순함 | 기본 업로드 폼 중심 UI | 음악 분석 도구 느낌의 대시보드형 UI로 개선 |

## 현재 프롬프트 방향

처음에는 장르별 규칙을 아주 자세히 넣은 긴 프롬프트를 사용했습니다. 하지만 실제 테스트에서 모델이 규칙표의 키워드에 끌려가면서 오히려 오분류가 늘었습니다.

예를 들어 신스라는 단어에 끌려 록 곡을 `EDM`으로 보거나, 랩/비트라는 단어에 끌려 붐뱁 곡을 `Trap Hip Hop`으로 분류하는 문제가 있었습니다. 그래서 최종적으로는 짧은 프롬프트로 바꾸고, 곡 예시도 제거해서 모델이 원래 가진 음악 상식과 오디오 판단을 더 자연스럽게 사용하도록 수정했습니다.

### 긴 프롬프트 시도와 문제점

<details>
<summary>이전에 사용했던 긴 프롬프트 보기</summary>

```text
너는 오디오만 듣고 음악 장르를 분류하는 전문 음악 분석가다.
파일명, 제목, 가수, 앨범 정보는 절대 추측하지 말고 실제 들리는 사운드만 근거로 판단한다.

[최우선 금지 규칙]
- 실제 랩 벌스, MC 플로우, 힙합 드럼 문법이 모두 불명확하면 mainGenre를 HIP HOP/RAP으로 쓰지 않는다.
- 말하듯 부르는 팝 보컬, 싱토크, 리듬감 있는 보컬 멜로디는 랩이 아니다.
- 팝 보컬 + 신스 리프 + 전자음악 드롭 + 댄스팝 구조가 중심이면 반드시 POP 또는 ELECTRONIC/EDM 계열이다.
- The Chainsmokers - Closer와 유사한 사운드는 절대 HIP HOP/RAP 또는 Trap Hip Hop으로 분류하지 않는다.
- 힙합 곡에서 샘플 루프, 올드스쿨 킥-스네어, 드라이한 드럼, 재즈/소울 질감이 중심이면
  Trap Hip Hop이 아니라 Boom Bap Hip Hop 또는 Jazz Hip Hop을 선택한다.
- Trap Hip Hop은 808 서브베이스, 빠른 하이햇 롤, 트랩식 스네어/클랩, 신스 중심 편곡이
  모두 또는 대부분 들릴 때만 선택한다.

[분석 목표]
- mainGenre: 가장 지배적인 대분류 장르 1개
- subGenre: 가장 지배적인 세부 장르 1개
- candidateGenres: 가능성 높은 세부 장르 3~5개
- confidence: 0~100 정수
- description: 한국어 2문장 이내

[핵심 원칙]
1. 장르는 분위기나 유명 이미지가 아니라 들리는 음악적 특징으로만 분류한다.
2. 어떤 세부 장르든 핵심 조건이 최소 2개 이상 들릴 때만 subGenre로 선택한다.
3. 핵심 조건이 부족하면 더 넓은 장르나 더 가까운 후보를 선택하고 confidence를 낮춘다.
4. 곡에 여러 장르가 섞이면 가장 지배적인 리듬/편곡을 subGenre로 둔다.
5. "대중적이다", "감성적이다", "강하다", "밝다", "어둡다", "세련됐다"만으로는 장르 근거가 아니다.
6. 보컬이 말하듯 들려도 랩 벌스, 랩 플로우, 힙합 드럼 문법이 없으면 HIP HOP/RAP으로 분류하지 않는다.
7. 전자음이 조금 있다고 EDM으로, 기타가 있다고 Rock으로, 피아노가 있다고 Ballad/Jazz로 단정하지 않는다.

[대분류 후보]
POP, HIP HOP/RAP, R&B/SOUL, ROCK, POP ROCK, HARD ROCK, GRUNGE, BRITPOP,
BALLAD, ROCK BALLAD, JAZZ, BLUES, ELECTRONIC/EDM, DANCE/DISCO,
FOLK/ACOUSTIC, METAL, PUNK, INDIE, ALTERNATIVE, REGGAE, CLASSICAL,
AMBIENT, EXPERIMENTAL, UNKNOWN

[장르별 핵심 조건]
POP:
- 선명한 보컬 멜로디, 반복 후렴, 대중적인 송폼, 깔끔한 편곡이 중심이면 Pop.
- 신스 리프와 전자음악 드롭이 중심이면 Electro Pop, Dance Pop, Future Bass를 우선 검토한다.

HIP HOP/RAP:
- 랩 벌스, 랩 플로우, MC 중심 구조, 힙합 드럼 문법이 명확할 때만 HIP HOP/RAP.
- Boom Bap Hip Hop: 샘플 루프, 묵직하거나 드라이한 킥/스네어, 올드스쿨 그루브.
- Jazz Hip Hop: 재즈 코드, Rhodes/피아노/브라스/베이스 질감, 따뜻한 샘플.
- Trap Hip Hop: 808 서브베이스, 빠른 하이햇 롤, 트랩식 스네어/클랩, 어두운 신스.

ROCK / POP ROCK / HARD ROCK:
- Rock은 밴드 편성, 드럼, 베이스, 기타 리프/코드, 록 보컬이 지배적일 때.
- Pop Rock은 록 밴드 사운드와 팝 멜로디/후렴이 균형을 이룰 때.
- Hard Rock은 묵직한 디스토션 기타 리프, 강한 드럼, 직선적인 록 보컬이 중심일 때.

ELECTRONIC/EDM / DANCE:
- Electronic/EDM은 신스, 드럼머신, 반복 루프, 빌드업/드롭, 클럽 믹스 구조가 지배적일 때.
- House는 4-on-the-floor 킥, 반복 그루브, 클럽 친화적 베이스/신스가 중심일 때.
- Future Bass는 밝은 신스 코드, 흔들리는 드롭, 팝 보컬과 전자 드롭이 중심일 때.

[자주 틀리는 케이스 방지]
- The Chainsmokers - Closer 같은 사운드는 Electro Pop, Dance Pop, Future Bass, Synth Pop을 우선한다.
- Louis Armstrong - What a Wonderful World 같은 사운드는 Vocal Jazz, Traditional Pop, Jazz Standard를 우선한다.
- Owell Mood - 작업 같은 사운드는 Boom Bap Hip Hop, Alternative Hip Hop, Jazz Hip Hop 후보를 우선한다.

[출력 규칙]
- MusicGenreResult JSON 필드만 채운다.
- mainGenre는 대분류 후보 중 하나만 사용한다.
- subGenre와 candidateGenres는 세부 장르 후보명을 사용한다.
- candidateGenres는 가장 가능성 높은 순서로 3~5개를 넣는다.
- description은 추측성 메타정보 없이 들리는 사운드 근거만 쓴다.
```

</details>

긴 프롬프트에서 확인한 문제:

- 규칙이 너무 많아 모델이 오디오보다 프롬프트 키워드에 끌림
- `신스`, `비트`, `랩`, `대중적` 같은 단어를 과하게 해석함
- 예외 케이스를 많이 넣을수록 다른 장르 판단이 흔들림
- 장르 사전처럼 작성하니 모델이 자연스럽게 듣고 판단하지 못함

### 최종 프롬프트 방향

성능 향상을 위해 현재는 짧은 프롬프트를 사용합니다.

- 오디오에서 들리는 리듬, 악기, 보컬/랩 방식, 편곡을 우선
- 파일명에 곡명이나 아티스트명이 있으면 보조 정보로 참고
- 유명곡이면 일반적으로 알려진 장르도 참고
- 특정 곡 예시는 넣지 않음
- 장르를 억지로 세분화하지 않고 사람이 자연스럽게 말할 장르를 선택
- 확신이 낮으면 후보 장르와 신뢰도를 함께 반환

<details>
<summary>현재 사용하는 짧은 프롬프트 보기</summary>

```text
너는 음악 장르를 판단하는 음악 분석가다.
첨부된 오디오를 듣고 가장 자연스러운 대표 장르와 세부 장르를 판단해라.

파일명: {업로드 파일명}

판단 기준:
- 오디오에서 실제로 들리는 리듬, 악기, 보컬/랩 방식, 편곡을 가장 중요하게 본다.
- 파일명에 곡명이나 아티스트명이 보이면 보조 정보로 참고해도 된다.
- 장르를 억지로 세분화하지 말고, 사람이 음악을 들었을 때 가장 자연스럽게 말할 장르를 고른다.
- 유명곡이면 일반적으로 알려진 장르도 참고한다.
- 확신이 낮거나 장르가 섞여 있으면 candidateGenres에 가까운 후보를 함께 넣는다.
- mainGenre는 넓은 대표 장르, subGenre는 더 구체적인 장르로 쓴다.
- candidateGenres는 가능성이 높은 순서로 3~5개를 넣고, 첫 번째 값은 subGenre와 같게 한다.
- confidence는 0~100 정수로 쓴다.
- description은 한국어 1~2문장으로, 왜 그렇게 판단했는지 짧게 쓴다.

응답은 MusicGenreResult 구조에 맞춰라.
```

</details>

## 테스트해본 대표 케이스

| 곡 | 기대 장르 |
| --- | --- |
| Jazzyfact - 아까워 | `Jazz Hip Hop`, `Boom Bap Hip Hop` |
| Owell Mood - 작업 | `Boom Bap Hip Hop`, `Alternative Hip Hop` |
| The Chainsmokers - Closer | `Electro Pop`, `Dance Pop`, `Future Bass` |
| Dave Brubeck - Take Five | `Jazz`, `Cool Jazz` |
| Nirvana - Smells Like Teen Spirit | `Grunge`, `Alternative Rock` |

## 실행

```bash
./gradlew.bat bootRun
```

기본 포트 `8080`이 사용 중이면 다음처럼 실행할 수 있습니다.

```bash
./gradlew.bat bootRun --args=--server.port=8081
```

접속 URL:

```text
http://localhost:8081/genre/upload
```

## 검증

```bash
./gradlew.bat -DskipTests compileJava
```

확인 결과:

```text
BUILD SUCCESSFUL
```

참고: `gradlew test`는 코드 컴파일 문제가 아니라 Gradle Test Executor 쪽에서 `GradleWorkerMain` 클래스를 찾지 못하는 실행 환경 문제가 발생했습니다.
