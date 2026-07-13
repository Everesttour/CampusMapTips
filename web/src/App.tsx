import { useEffect, useMemo, useState } from 'react'
import {
  Building2,
  ChevronRight,
  Clock3,
  Info,
  Layers3,
  MapPin,
  MessageSquarePlus,
  Minus,
  Navigation,
  Plus,
  Search,
  Sparkles,
  X,
} from 'lucide-react'
import './App.css'

type PlaceKind = 'building' | 'facility'

type Building = {
  id: number
  xLocation: number
  yLocation: number
  name: string
  description: string
  rooms: string
}

type Facility = {
  id: number
  xLocation: number
  yLocation: number
  floor: number
  name: string
  buildingName: string
  overview: string
  description: string
  notice: string
  operatingHours: string
  tips: Array<string | null>
}

type Selection =
  | { kind: 'building'; place: Building }
  | { kind: 'facility'; place: Facility }
  | null

const MAP_WIDTH = 1600
const MAP_HEIGHT = 1125
const repositoryUrl = 'https://github.com/Everesttour/CampusMapTips'

const placeMeta = {
  building: {
    label: '건물',
    plural: '건물 안내',
    icon: Building2,
  },
  facility: {
    label: '시설',
    plural: '시설·꿀팁',
    icon: Sparkles,
  },
} as const

function assetPath(path: string) {
  return `${import.meta.env.BASE_URL}${path}`
}

async function loadJson<T>(path: string): Promise<T> {
  const response = await fetch(assetPath(path))
  if (!response.ok) {
    throw new Error('안내 데이터를 불러오지 못했습니다.')
  }

  return response.json() as Promise<T>
}

function containsQuery(place: Building | Facility, query: string) {
  const normalizedQuery = query.trim().toLocaleLowerCase('ko-KR')
  if (!normalizedQuery) return true

  const searchable = [
    place.name,
    place.description,
    'overview' in place ? place.overview : '',
    'buildingName' in place ? place.buildingName : '',
    'rooms' in place ? place.rooms : '',
  ]
    .join(' ')
    .toLocaleLowerCase('ko-KR')

  return searchable.includes(normalizedQuery)
}

function floorLabel(floor: number) {
  if (floor === 0) return '공용 공간'
  return floor < 0 ? `B${Math.abs(floor)}층` : `${floor}층`
}

function photoPath(selection: Exclude<Selection, null>) {
  const folder = selection.kind === 'building' ? 'buildings' : 'facilities'
  return assetPath(`images/${folder}/${selection.place.id}.webp`)
}

function App() {
  const [buildings, setBuildings] = useState<Building[]>([])
  const [facilities, setFacilities] = useState<Facility[]>([])
  const [activeKind, setActiveKind] = useState<PlaceKind>('building')
  const [query, setQuery] = useState('')
  const [selection, setSelection] = useState<Selection>(null)
  const [showFacilityPins, setShowFacilityPins] = useState(false)
  const [mapScale, setMapScale] = useState(1)
  const [loadError, setLoadError] = useState('')

  useEffect(() => {
    Promise.all([
      loadJson<Building[]>('data/buildings.json'),
      loadJson<Facility[]>('data/facilities.json'),
    ])
      .then(([buildingData, facilityData]) => {
        setBuildings(buildingData.sort((a, b) => a.id - b.id))
        setFacilities(facilityData.sort((a, b) => a.id - b.id))
      })
      .catch((error: unknown) => {
        setLoadError(error instanceof Error ? error.message : '안내 데이터를 불러오지 못했습니다.')
      })
  }, [])

  const displayedPlaces = useMemo(() => {
    const source = activeKind === 'building' ? buildings : facilities
    return source.filter((place) => containsQuery(place, query))
  }, [activeKind, buildings, facilities, query])

  const selectPlace = (kind: PlaceKind, place: Building | Facility) => {
    if (kind === 'facility') setShowFacilityPins(true)
    setSelection(kind === 'building' ? { kind, place: place as Building } : { kind, place: place as Facility })
  }

  const chooseKind = (kind: PlaceKind) => {
    setActiveKind(kind)
    if (kind === 'facility') setShowFacilityPins(true)
  }

  const isLoading = !loadError && buildings.length === 0 && facilities.length === 0

  if (isLoading) {
    return <main className="loading-screen">캠퍼스 지도를 준비하고 있습니다.</main>
  }

  if (loadError) {
    return (
      <main className="loading-screen error-state">
        <Info size={22} />
        <p>{loadError}</p>
        <a href={repositoryUrl}>저장소에서 데이터 확인하기</a>
      </main>
    )
  }

  const ActiveIcon = placeMeta[activeKind].icon

  return (
    <main className="app-shell">
      <aside className="navigation-panel" aria-label="장소 탐색">
        <div className="brand-block">
          <div className="brand-mark" aria-hidden="true">
            <Navigation size={20} strokeWidth={2.4} />
          </div>
          <div>
            <p className="eyebrow">SUCCESS CAMPUS</p>
            <h1>캠퍼스 꿀팁 지도</h1>
          </div>
        </div>

        <p className="intro">처음 가는 장소도, 자주 가는 시설도 한눈에 찾아보세요.</p>

        <label className="search-field">
          <Search size={18} aria-hidden="true" />
          <input
            value={query}
            onChange={(event) => setQuery(event.target.value)}
            placeholder="건물, 시설, 키워드 검색"
            aria-label="건물과 시설 검색"
          />
          {query && (
            <button className="clear-search" type="button" onClick={() => setQuery('')} aria-label="검색어 지우기">
              <X size={16} />
            </button>
          )}
        </label>

        <div className="kind-tabs" role="tablist" aria-label="안내 종류">
          {(Object.keys(placeMeta) as PlaceKind[]).map((kind) => {
            const Icon = placeMeta[kind].icon
            const count = kind === 'building' ? buildings.length : facilities.length
            const isActive = activeKind === kind

            return (
              <button
                className={`kind-tab ${isActive ? 'is-active' : ''}`}
                type="button"
                role="tab"
                aria-selected={isActive}
                key={kind}
                onClick={() => chooseKind(kind)}
              >
                <Icon size={17} aria-hidden="true" />
                <span>{placeMeta[kind].label}</span>
                <em>{count}</em>
              </button>
            )
          })}
        </div>

        <div className="result-heading">
          <div>
            <p>{placeMeta[activeKind].plural}</p>
            <strong>{query ? `'${query}' 검색 결과` : '지도를 눌러 바로 확인'}</strong>
          </div>
          <span>{displayedPlaces.length}</span>
        </div>

        <div className="place-list" role="list">
          {displayedPlaces.map((place) => {
            const selected = selection?.kind === activeKind && selection.place.id === place.id
            const subtitle = activeKind === 'building'
              ? (place as Building).description
              : `${(place as Facility).buildingName || '교외 시설'} · ${floorLabel((place as Facility).floor)}`

            return (
              <button
                type="button"
                role="listitem"
                className={`place-row ${selected ? 'is-selected' : ''}`}
                key={place.id}
                onClick={() => selectPlace(activeKind, place)}
              >
                <span className={`place-icon ${activeKind}`} aria-hidden="true">
                  <ActiveIcon size={17} />
                </span>
                <span className="place-copy">
                  <strong>{place.name}</strong>
                  <small>{subtitle}</small>
                </span>
                <ChevronRight size={17} aria-hidden="true" />
              </button>
            )
          })}
          {displayedPlaces.length === 0 && (
            <div className="empty-results">
              <Search size={20} aria-hidden="true" />
              <p>찾는 장소가 없어요.</p>
              <button type="button" onClick={() => setQuery('')}>검색어 지우기</button>
            </div>
          )}
        </div>

        <a className="suggest-link" href={`${repositoryUrl}/issues/new`} target="_blank" rel="noreferrer">
          <MessageSquarePlus size={17} aria-hidden="true" />
          정보 수정 제안하기
          <ChevronRight size={16} aria-hidden="true" />
        </a>
      </aside>

      <section className="map-panel" aria-label="성공회대학교 캠퍼스 지도">
        <header className="map-header">
          <div>
            <p className="eyebrow">CAMPUS GUIDE</p>
            <h2>오늘 필요한 곳을 찾아볼까요?</h2>
          </div>
          <div className="map-actions">
            <button
              className={`facility-switch ${showFacilityPins ? 'is-on' : ''}`}
              type="button"
              onClick={() => setShowFacilityPins((current) => !current)}
              aria-pressed={showFacilityPins}
            >
              <Layers3 size={16} aria-hidden="true" />
              시설 핀
            </button>
            <div className="zoom-control" aria-label="지도 확대 축소">
              <button type="button" onClick={() => setMapScale((current) => Math.max(1, Number((current - 0.15).toFixed(2))))} disabled={mapScale <= 1} aria-label="지도 축소">
                <Minus size={16} />
              </button>
              <output>{Math.round(mapScale * 100)}%</output>
              <button type="button" onClick={() => setMapScale((current) => Math.min(1.45, Number((current + 0.15).toFixed(2))))} disabled={mapScale >= 1.45} aria-label="지도 확대">
                <Plus size={16} />
              </button>
            </div>
          </div>
        </header>

        <div className="map-stage">
          <div className="map-viewport">
            <div className="map-canvas" style={{ transform: `scale(${mapScale})` }}>
              <img className="campus-map" src={assetPath('images/map.webp')} alt="성공회대학교 캠퍼스 조감 지도" draggable="false" />

              {buildings.map((building) => {
                const isSelected = selection?.kind === 'building' && selection.place.id === building.id
                return (
                  <button
                    className={`building-target ${isSelected ? 'is-selected' : ''}`}
                    type="button"
                    key={building.id}
                    style={{ left: `${(building.xLocation / MAP_WIDTH) * 100}%`, top: `${(building.yLocation / MAP_HEIGHT) * 100}%` }}
                    onClick={() => selectPlace('building', building)}
                    aria-label={`${building.name} 상세 보기`}
                  >
                    <span>{String(building.id).padStart(2, '0')}</span>
                  </button>
                )
              })}

              {showFacilityPins && facilities.map((facility) => {
                const isSelected = selection?.kind === 'facility' && selection.place.id === facility.id
                return (
                  <button
                    className={`facility-pin ${isSelected ? 'is-selected' : ''}`}
                    type="button"
                    key={facility.id}
                    style={{ left: `${(facility.xLocation / MAP_WIDTH) * 100}%`, top: `${(facility.yLocation / MAP_HEIGHT) * 100}%` }}
                    onClick={() => selectPlace('facility', facility)}
                    aria-label={`${facility.name} 상세 보기`}
                  >
                    <span>{facility.name}</span>
                  </button>
                )
              })}
            </div>
          </div>

          <div className="map-legend" aria-label="지도 범례">
            <span><i className="building-dot" /> 건물</span>
            <span><i className="facility-dot" /> 시설·꿀팁</span>
          </div>

          {selection && (
            <PlaceDetail
              selection={selection}
              onClose={() => setSelection(null)}
            />
          )}
        </div>

        <footer className="map-footer">
          <Info size={15} aria-hidden="true" />
          정보는 변경될 수 있습니다. 방문 전 운영시간을 한 번 더 확인해 주세요.
        </footer>
      </section>
    </main>
  )
}

function PlaceDetail({ selection, onClose }: { selection: Exclude<Selection, null>; onClose: () => void }) {
  const { kind, place } = selection
  const isFacility = kind === 'facility'
  const facility = isFacility ? place as Facility : null
  const building = !isFacility ? place as Building : null
  const suggestionsUrl = `${repositoryUrl}/issues/new?title=${encodeURIComponent(`${place.name} 정보 수정 제안`)}`
  const tips = facility?.tips.filter((tip): tip is string => Boolean(tip?.trim())) ?? []

  return (
    <aside className="detail-panel" aria-label={`${place.name} 상세 정보`}>
      <button className="close-detail" type="button" onClick={onClose} aria-label="상세 정보 닫기">
        <X size={18} />
      </button>
      <div className="detail-visual">
        <img src={photoPath(selection)} alt="" onError={(event) => event.currentTarget.parentElement?.classList.add('image-unavailable')} />
      </div>
      <div className="detail-content">
        <span className={`detail-badge ${kind}`}>{placeMeta[kind].label}</span>
        <h3>{place.name}</h3>
        {isFacility && facility && (
          <p className="detail-location"><MapPin size={15} aria-hidden="true" /> {facility.buildingName || '교외 시설'} · {floorLabel(facility.floor)}</p>
        )}
        {facility?.overview && <p className="overview">{facility.overview}</p>}
        <p className="detail-description">{place.description}</p>

        {building?.rooms && (
          <section className="detail-section">
            <h4><Building2 size={16} aria-hidden="true" /> 주요 공간</h4>
            <p>{building.rooms}</p>
          </section>
        )}

        {facility?.operatingHours && (
          <section className="detail-section">
            <h4><Clock3 size={16} aria-hidden="true" /> 운영 안내</h4>
            <p>{facility.operatingHours}</p>
          </section>
        )}

        {facility?.notice && (
          <section className="notice-box">
            <Info size={16} aria-hidden="true" />
            <p>{facility.notice}</p>
          </section>
        )}

        {tips.length > 0 && (
          <section className="tips-section">
            <h4><Sparkles size={16} aria-hidden="true" /> 학생 꿀팁</h4>
            <ul>
              {tips.map((tip, index) => <li key={`${tip}-${index}`}>{tip}</li>)}
            </ul>
          </section>
        )}

        <a className="detail-suggest" href={suggestionsUrl} target="_blank" rel="noreferrer">
          <MessageSquarePlus size={16} aria-hidden="true" />
          정보 수정 제안
        </a>
      </div>
    </aside>
  )
}

export default App
