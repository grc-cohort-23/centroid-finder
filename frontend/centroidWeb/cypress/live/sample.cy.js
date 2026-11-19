// Opt-in smoke tests for the local sample and a short H.264 test clip.
function chooseVideo(filename) {
  cy.contains('li', filename).find('button').click()
}

function setThresholdTo150() {
  cy.get('input[type=range]').then(($input) => {
    const input = $input[0]
    const win = input.ownerDocument.defaultView
    Object.getOwnPropertyDescriptor(win.HTMLInputElement.prototype, 'value').set.call(input, '150')
    input.dispatchEvent(new win.Event('input', { bubbles: true }))
  }).should('have.value', '150')
}

it('renders both real frames and the expected centroid for ensantina.mp4', () => {
  cy.visit('/videos')
  cy.contains('li', 'ensantina.mp4').should('be.visible')
  cy.screenshot('video-selection', { capture: 'viewport' })
  chooseVideo('ensantina.mp4')
  cy.location('pathname').should('eq', '/preview/ensantina.mp4')
  cy.get('img[alt="Binarized frame"]', { timeout: 15000 }).then(($initialImage) => {
    const initialUrl = $initialImage.attr('src')
    setThresholdTo150()
    cy.get('img[alt="Binarized frame"]', { timeout: 15000 }).should('not.have.attr', 'src', initialUrl)
  })
  cy.contains('Centroid (415, 367)', { timeout: 15000 }).should('be.visible')
  cy.get('.frame-image img').should('have.length', 2).each(($image) => {
    cy.wrap($image).should(($loaded) => {
      expect($loaded[0].naturalWidth).to.equal(816)
      expect($loaded[0].naturalHeight).to.equal(720)
    })
  })
  cy.get('.centroid-marker').should('have.length', 2).each(($marker) => {
    const marker = $marker[0].getBoundingClientRect()
    const image = $marker.siblings('img')[0].getBoundingClientRect()
    expect(marker.left + marker.width / 2 - image.left).to.be.closeTo(image.width * 415.5 / 816, 1)
    expect(marker.top + marker.height / 2 - image.top).to.be.closeTo(image.height * 367.5 / 720, 1)
  })
  cy.screenshot('preview-processing', { capture: 'fullPage' })
})

it('processes a short video through the Java JAR and downloads its CSV', () => {
  cy.visit('/videos')
  chooseVideo('short-check.mp4')
  cy.contains('button', 'Process Video with These Settings').click()
  cy.contains('Processing complete.', { timeout: 30000 }).should('be.visible')
  cy.contains('a', 'Download CSV').then(($link) => {
    const csv = $link.attr('href')
    cy.wrap($link).click()
    cy.readFile(`cypress/downloads/${csv.split('/').at(-1)}`, { timeout: 10000 }).should((contents) => {
      const rows = contents.trim().split(/\r?\n/)
      expect(rows).to.have.length(3)
      rows.forEach((row, index) => expect(row).to.match(new RegExp(`^${index}\\.0,-?\\d+,-?\\d+$`)))
    })
  })
})
